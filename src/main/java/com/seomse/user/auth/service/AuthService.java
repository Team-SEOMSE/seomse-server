package com.seomse.user.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.common.exception.DuplicateEmailException;
import com.seomse.security.jwt.JwtTokenGenerator;
import com.seomse.security.jwt.dto.JwtToken;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.request.EmailCheckServiceRequest;
import com.seomse.user.auth.service.request.LoginServiceRequest;
import com.seomse.user.auth.service.request.SignupServiceRequest;
import com.seomse.user.auth.service.response.EmailCheckResponse;
import com.seomse.user.auth.service.response.LoginResponse;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthService {

	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final JwtTokenGenerator jwtTokenGenerator;

	private final ClientRepository clientRepository;

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
			case DESIGNER, OWNER -> null; // merge 후 개발
		};
	}

	public EmailCheckResponse emailExists(EmailCheckServiceRequest request) {
		return new EmailCheckResponse(emailExists(request.email(), request.role()));
	}

	public boolean emailExists(String email, Role role) {
		return switch (role) {
			case CLIENT -> clientRepository.findByEmail(email).isPresent();
			case DESIGNER, OWNER -> false; // merge 후 개발
		};
	}
}
