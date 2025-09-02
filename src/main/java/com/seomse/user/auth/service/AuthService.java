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
		ClientEntity client = clientRepository.findByEmail(request.email())
			.orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

		if (!bCryptPasswordEncoder.matches(request.password(), client.getPassword())) {
			throw new IllegalArgumentException("Invalid email or password.");
		}

		LoginUserInfo userInfo = new LoginUserInfo(client.getId(), request.role());
		JwtToken jwtToken = jwtTokenGenerator.generate(userInfo);

		return new LoginResponse(jwtToken.accessToken());
	}

	public UUID signup(SignupServiceRequest request) {
		if (emailExists(request.email(), request.role())) {
			throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + request.email());
		}

		return switch (request.role()) {
			case CLIENT -> {
				ClientEntity client = request.toClientEntity(bCryptPasswordEncoder);
				ClientEntity saved = clientRepository.save(client);
				yield saved.getId();
			}
			case DESIGNER, OWNER -> null; // 추후 개발
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
		String accessToken = client.getToken(kakaoClientId, kakaoRedirectUri, request.code(), kakaoClientSecret);
		String email = client.getEmail(accessToken);
		String nickname = client.getNickname(accessToken);

		ClientAndStatus result = findOrCreateClient(email, nickname, request.snsType());

		LoginUserInfo userInfo = new LoginUserInfo(result.client().getId(), Role.CLIENT);
		JwtToken jwtToken = jwtTokenGenerator.generate(userInfo);

		return new OauthLoginResponse(jwtToken.accessToken(), result.isNew());
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
