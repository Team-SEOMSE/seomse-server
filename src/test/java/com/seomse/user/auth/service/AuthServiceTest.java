package com.seomse.user.auth.service;

import static com.seomse.user.client.entity.ClientTestFactory.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.IntegrationTestSupport;
import com.seomse.security.jwt.JwtTokenGenerator;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.request.LoginServiceRequest;
import com.seomse.user.auth.service.response.LoginResponse;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.repository.ClientQueryRepository;
import com.seomse.user.client.repository.ClientRepository;

class AuthServiceTest extends IntegrationTestSupport {

	@Autowired
	private AuthService authService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private JwtTokenGenerator jwtTokenGenerator;

	@Autowired
	private ClientRepository clientRepository;
	@Autowired
	private ClientQueryRepository clientQueryRepository;

	@AfterEach
	void tearDown() {
		clientRepository.deleteAllInBatch();
	}

	@DisplayName("이메일로 회원 조회 후 비밀번호 검증에 성공하면 JWT 발급한다.")
	@Test
	void normalLogin() throws JsonProcessingException {
		// given
		String email = "user@email.com";
		String password = "abc1234!";

		ClientEntity client = newClient(bCryptPasswordEncoder, email, password, SnsType.NORMAL, Gender.MALE,
			Age.TWENTIES);

		clientRepository.save(client);

		LoginServiceRequest request = new LoginServiceRequest(email, password, Role.CLIENT);

		// when
		LoginResponse response = authService.normalLogin(request);

		// then
		Assertions.assertThat(response.accessToken()).isNotNull();
	}
}