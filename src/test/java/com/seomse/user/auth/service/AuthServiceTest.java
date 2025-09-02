package com.seomse.user.auth.service;

import static org.mockito.ArgumentMatchers.*;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.IntegrationTestSupport;
import com.seomse.common.exception.DuplicateEmailException;
import com.seomse.security.feign.kakao.response.KakaoTokenResponse;
import com.seomse.security.feign.kakao.response.KakaoUserInfoResponse;
import com.seomse.user.auth.enums.Role;
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

class AuthServiceTest extends IntegrationTestSupport {

	@Autowired
	private AuthService authService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ClientRepository clientRepository;

	@AfterEach
	void tearDown() {
		clientRepository.deleteAllInBatch();
	}

	@DisplayName("이메일로 회원 조회 후 비밀번호 검증에 성공하면 인증 토큰을 발급한다.")
	@Test
	void normalLogin() throws JsonProcessingException {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, null,
			null);

		clientRepository.save(client);

		LoginServiceRequest request = new LoginServiceRequest(email, password, Role.CLIENT);

		// when
		LoginResponse response = authService.normalLogin(request);

		// then
		Assertions.assertThat(response.accessToken()).isNotNull();
	}

	@DisplayName("이메일 중복 체크를 하고 Role에 따라 맞는 테이블에 저장한다.")
	@Test
	void signup() {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;
		Role role = Role.CLIENT;

		SignupServiceRequest request = new SignupServiceRequest(email, password, name, snsType, role);

		// when
		UUID result = authService.signup(request);

		// then
		Assertions.assertThat(result).isNotNull();
	}

	@DisplayName("회원가입 시 이메일이 중복이면 예외처리를 한다.")
	@Test
	void signup_duplicateEmail() {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;
		Role role = Role.CLIENT;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, null,
			null);

		clientRepository.save(client);

		SignupServiceRequest request = new SignupServiceRequest(email, password, name, snsType, role);

		// when // then
		Assertions.assertThatThrownBy(() -> authService.signup(request))
			.isInstanceOf(DuplicateEmailException.class)
			.hasMessage("이미 사용 중인 이메일입니다: " + email);
	}

	@DisplayName("이메일 중복 체크를 한다.")
	@Test
	void emailExists() {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;
		Role role = Role.CLIENT;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, null,
			null);

		clientRepository.save(client);

		EmailCheckServiceRequest request = new EmailCheckServiceRequest(email, role);

		// when // then
		EmailCheckResponse result = authService.emailExists(request);

		Assertions.assertThat(result.duplicate()).isTrue();
	}

	@Transactional
	@DisplayName("카카오 로그인을 한다.")
	@Test
	void oauthKakaoLoginTest() throws JsonProcessingException {
		// given
		KakaoUserInfoResponse.KakaoAccount kakaoAccount = new KakaoUserInfoResponse.KakaoAccount(
			true, false, true, true, "test@email.com"
		);
		KakaoUserInfoResponse.Properties properties = new KakaoUserInfoResponse.Properties("김섬세");

		BDDMockito.given(kakaoApiFeignCall.getUserInfo(anyString()))
			.willReturn(new KakaoUserInfoResponse(1L, "2025-09-01T12:58:00Z", kakaoAccount, properties));

		BDDMockito.given(kakaoAuthFeignCall.getToken(anyString(), anyString(), anyString(), anyString(), anyString()))
			.willReturn(new KakaoTokenResponse("token", "bearer", null, null, null));

		OauthLoginServiceRequest request = new OauthLoginServiceRequest("ASDJKSAN", SnsType.KAKAO);

		// when
		OauthLoginResponse response = authService.oauthLogin(request);

		Assertions.assertThat(response.accessToken()).isNotNull();
		Assertions.assertThat(response.isNew()).isTrue();
	}
}