package com.seomse.user.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.security.jwt.JwtTokenGenerator;
import com.seomse.security.jwt.dto.JwtToken;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.user.auth.service.request.LoginServiceRequest;
import com.seomse.user.auth.service.response.LoginResponse;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.repository.ClientQueryRepository;
import com.seomse.user.client.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthService {

	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final JwtTokenGenerator jwtTokenGenerator;

	private final ClientRepository clientRepository;
	private final ClientQueryRepository clientQueryRepository;

	public LoginResponse normalLogin(LoginServiceRequest request) throws JsonProcessingException {
		ClientEntity client = clientQueryRepository.findByEmail(request.email())
			.orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

		if (!bCryptPasswordEncoder.matches(request.password(), client.getPassword())) {
			throw new IllegalArgumentException("Invalid email or password.");
		}

		LoginUserInfo userInfo = new LoginUserInfo(client.getId(), request.role());
		JwtToken jwtToken = jwtTokenGenerator.generate(userInfo);

		return new LoginResponse(jwtToken.accessToken());
	}
}
