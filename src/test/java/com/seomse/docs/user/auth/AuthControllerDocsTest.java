package com.seomse.docs.user.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.seomse.docs.RestDocsSupport;
import com.seomse.user.auth.controller.AuthController;
import com.seomse.user.auth.controller.request.EmailCheckRequest;
import com.seomse.user.auth.controller.request.LoginRequest;
import com.seomse.user.auth.controller.request.SignupRequest;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.AuthService;
import com.seomse.user.auth.service.request.EmailCheckServiceRequest;
import com.seomse.user.auth.service.request.LoginServiceRequest;
import com.seomse.user.auth.service.request.SignupServiceRequest;
import com.seomse.user.auth.service.response.EmailCheckResponse;
import com.seomse.user.auth.service.response.LoginResponse;
import com.seomse.user.client.enums.SnsType;

public class AuthControllerDocsTest extends RestDocsSupport {

	private final AuthService authService = Mockito.mock(AuthService.class);

	@Override
	protected Object initController() {
		return new AuthController(authService);
	}

	@DisplayName("일반 로그인 API")
	@Test
	void normalLogin() throws Exception {
		// given
		LoginRequest request = new LoginRequest("user@email.com", "abc1234!", Role.CLIENT);

		given(authService.normalLogin(any(LoginServiceRequest.class)))
			.willReturn(new LoginResponse(
				"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJ1c2VySWRcIjpcImY4MWQ0ZmFlLTdkZWMtMTFkMC1hNzY1LTAwYTBjO"
					+ "TFlNmJmNlwiLFwicm9sZVwiOlwiQ0xJRU5UXCJ9IiwiZXhwIjoxNzU1OTY3MTA4LCJVVUlEIjoiOTdhOWRlMWItNGFiNS00"
					+ "N2E1LWJkMDktMTc5MGUxMDc4NWY1In0.Fz9O6EfhonoWspVddRofnw7IoXCmkXrgseSCZajMU1-OqYXxq82I_U1x9Xfgc9z"
					+ "cYv-pHLKMTJXGqoTegWuN4A")
			);

		// when // then
		mockMvc.perform(
				post("/user/auth/login")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("auth-normalLogin",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("email").type(JsonFieldType.STRING)
						.description("이메일"),
					fieldWithPath("password").type(JsonFieldType.STRING)
						.description("비밀번호"),
					fieldWithPath("role").type(JsonFieldType.STRING)
						.description("로그인한 유저의 역할. 가능한 값: " + Arrays.toString(Role.values()))
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("코드"),
					fieldWithPath("data").type(JsonFieldType.OBJECT)
						.description("응답 데이터"),
					fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
						.description("인증 토큰")
				)
			));
	}

	@DisplayName("회원가입 API")
	@Test
	void signup() throws Exception {
		// given
		SignupRequest request = new SignupRequest("user@email.com", "abc1234!", SnsType.NORMAL, Role.CLIENT);

		given(authService.signup(any(SignupServiceRequest.class)))
			.willReturn(UUID.randomUUID());

		// when // then
		mockMvc.perform(
				post("/user/auth/signup")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andDo(document("auth-signup",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestFields(
					fieldWithPath("email").type(JsonFieldType.STRING)
						.description("이메일"),
					fieldWithPath("password").type(JsonFieldType.STRING)
						.description("비밀번호"),
					fieldWithPath("snsType").type(JsonFieldType.STRING)
						.description("가입하는 sns 유형. 가능한 값: " + Arrays.toString(SnsType.values())),
					fieldWithPath("role").type(JsonFieldType.STRING)
						.description("가입하는 유저의 역할. 가능한 값: " + Arrays.toString(Role.values()))
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("코드"),
					fieldWithPath("data").type(JsonFieldType.STRING)
						.description("응답 데이터, 저장된 사용자 ID")
				)
			));
	}

	@DisplayName("이메일 중복 확인 API")
	@Test
	void checkEmail() throws Exception {
		// given
		EmailCheckRequest request = new EmailCheckRequest("user@email.com", Role.CLIENT);

		given(authService.emailExists(any(EmailCheckServiceRequest.class)))
			.willReturn(new EmailCheckResponse(false));

		// when // then
		mockMvc.perform(
				get("/user/auth/check")
					.param("email", request.email())
					.param("role", request.role().name())
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.duplicate").value(false))
			.andDo(document("auth-checkEmail",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("email").description("중복을 확인할 이메일"),
					parameterWithName("role").description("유저 역할. 가능한 값: " + Arrays.toString(Role.values()))
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("코드"),
					fieldWithPath("data.duplicate").type(JsonFieldType.BOOLEAN)
						.description("중복 여부. true=이미 사용 중, false=사용 가능")
				)
			));
	}
}
