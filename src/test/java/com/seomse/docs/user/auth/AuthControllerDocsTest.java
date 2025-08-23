package com.seomse.docs.user.auth;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.seomse.docs.RestDocsSupport;
import com.seomse.user.auth.controller.AuthController;
import com.seomse.user.auth.controller.request.LoginRequest;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.AuthService;
import com.seomse.user.auth.service.request.LoginServiceRequest;
import com.seomse.user.auth.service.response.LoginResponse;

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

		BDDMockito.given(authService.normalLogin(ArgumentMatchers.any(LoginServiceRequest.class)))
			.willReturn(new LoginResponse(
				"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XCJ1c2VySWRcIjpcImY4MWQ0ZmFlLTdkZWMtMTFkMC1hNzY1LTAwYTBjO"
					+ "TFlNmJmNlwiLFwicm9sZVwiOlwiQ0xJRU5UXCJ9IiwiZXhwIjoxNzU1OTY3MTA4LCJVVUlEIjoiOTdhOWRlMWItNGFiNS00"
					+ "N2E1LWJkMDktMTc5MGUxMDc4NWY1In0.Fz9O6EfhonoWspVddRofnw7IoXCmkXrgseSCZajMU1-OqYXxq82I_U1x9Xfgc9z"
					+ "cYv-pHLKMTJXGqoTegWuN4A")
			);

		// when // then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/user/auth/login")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("auth-normalLogin",
				preprocessRequest(Preprocessors.prettyPrint()),
				preprocessResponse(Preprocessors.prettyPrint()),
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

}
