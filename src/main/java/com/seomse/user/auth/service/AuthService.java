package com.seomse.user.auth.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.common.exception.DuplicateEmailException;
import com.seomse.security.feign.kakao.client.OauthApiClient;
import com.seomse.security.jwt.JwtTokenGenerator;
import com.seomse.security.jwt.dto.JwtToken;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.dto.ClientAndStatus;
import com.seomse.user.auth.service.dto.UserAuthInfo;
import com.seomse.user.auth.service.request.EmailCheckServiceRequest;
import com.seomse.user.auth.service.request.LoginServiceRequest;
import com.seomse.user.auth.service.request.OauthLoginServiceRequest;
import com.seomse.user.auth.service.request.SignupServiceRequest;
import com.seomse.user.auth.service.response.EmailCheckResponse;
import com.seomse.user.auth.service.response.LoginResponse;
import com.seomse.user.auth.service.response.OauthLoginResponse;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.repository.ClientRepository;
import com.seomse.user.designer.repository.DesignerRepository;
import com.seomse.user.owner.repository.OwnerRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthService {

	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final JwtTokenGenerator jwtTokenGenerator;
	private final List<OauthApiClient> clientsList;

	private Map<SnsType, OauthApiClient> clients;

	private final ClientRepository clientRepository;
	private final DesignerRepository designerRepository;
	private final OwnerRepository ownerRepository;

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String kakaoClientId;

	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String kakaoClientSecret;

	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	private String kakaoRedirectUri;

	@PostConstruct
	private void initializeClients() {
		this.clients = clientsList.stream().collect(
			Collectors.toUnmodifiableMap(OauthApiClient::oauthSnsType, Function.identity())
		);
	}

	public LoginResponse normalLogin(LoginServiceRequest request) throws JsonProcessingException {

		final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password.";

		UserAuthInfo authInfo = switch (request.role()) {
			case CLIENT -> clientRepository.findByEmail(request.email())
				.map(user -> new UserAuthInfo(user.getId(), user.getPassword()))
				.orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS_MESSAGE));
			case DESIGNER -> designerRepository.findByEmail(request.email())
				.map(user -> new UserAuthInfo(user.getId(), user.getPassword()))
				.orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS_MESSAGE));
			case OWNER -> ownerRepository.findByEmail(request.email())
				.map(user -> new UserAuthInfo(user.getId(), user.getPassword()))
				.orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS_MESSAGE));
		};

		if (!bCryptPasswordEncoder.matches(request.password(), authInfo.encryptedPassword())) {
			throw new IllegalArgumentException(INVALID_CREDENTIALS_MESSAGE);
		}

		String accessToken = generateAccessToken(authInfo.id(), request.role());

		return new LoginResponse(accessToken);
	}

	public LoginResponse signup(SignupServiceRequest request) throws JsonProcessingException {
		if (emailExists(request.email(), request.role())) {
			throw new DuplicateEmailException("This email is already in use: " + request.email());
		}

		return switch (request.role()) {
			case CLIENT -> {
				ClientEntity client = request.toClientEntity(bCryptPasswordEncoder);
				ClientEntity savedClient = clientRepository.save(client);
				String accessToken = generateAccessToken(savedClient.getId(), request.role());
				yield new LoginResponse(accessToken);
			}
			case DESIGNER, OWNER ->
				throw new UnsupportedOperationException("Designer and Owner roles are not yet implemented.");
		};
	}

	public EmailCheckResponse emailExists(EmailCheckServiceRequest request) {
		return new EmailCheckResponse(emailExists(request.email(), request.role()));
	}

	public boolean emailExists(String email, Role role) {
		return switch (role) {
			case CLIENT -> clientRepository.findByEmail(email).isPresent();
			case DESIGNER, OWNER -> false; // 추후 개발
		};
	}

	public OauthLoginResponse oauthLogin(OauthLoginServiceRequest request) throws JsonProcessingException {
		OauthApiClient client = clients.get(request.snsType());
		String KaKaoAccessToken = client.getToken(kakaoClientId, kakaoRedirectUri, request.code(), kakaoClientSecret);
		String email = client.getEmail(KaKaoAccessToken);
		String nickname = client.getNickname(KaKaoAccessToken);

		ClientAndStatus result = findOrCreateClient(email, nickname, request.snsType());

		String accessToken = generateAccessToken(result.client().getId(), Role.CLIENT);
		return new OauthLoginResponse(accessToken, result.isNew());
	}

	private String generateAccessToken(UUID userId, Role role) throws JsonProcessingException {
		LoginUserInfo userInfo = new LoginUserInfo(userId, role);
		JwtToken jwtToken = jwtTokenGenerator.generate(userInfo);
		return jwtToken.accessToken();
	}

	private ClientAndStatus findOrCreateClient(String email, String nickname, SnsType snsType) {
		Optional<ClientEntity> optionalClient = clientRepository.findByEmail(email);

		if (optionalClient.isPresent()) {
			ClientEntity clientEntity = optionalClient.get();
			boolean isProfileComplete = clientEntity.getAge() != null && clientEntity.getGender() != null;
			return new ClientAndStatus(clientEntity, !isProfileComplete);
		} else {
			ClientEntity newClient = new ClientEntity(email, null, nickname, snsType, null, null);
			clientRepository.save(newClient);
			return new ClientAndStatus(newClient, true);
		}
	}
}
