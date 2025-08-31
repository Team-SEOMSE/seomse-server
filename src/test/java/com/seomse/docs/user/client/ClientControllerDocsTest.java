package com.seomse.docs.user.client;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.seomse.docs.RestDocsSupport;
import com.seomse.user.client.controller.ClientController;
import com.seomse.user.client.controller.request.UserProfileUpdateRequest;
import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.service.ClientService;
import com.seomse.user.client.service.request.UserProfileUpdateServiceRequest;
import com.seomse.user.client.service.response.UserProfileResponse;

public class ClientControllerDocsTest extends RestDocsSupport {

	private final ClientService clientService = Mockito.mock(ClientService.class);

	@Override
	protected Object initController() {
		return new ClientController(clientService);
	}

	@DisplayName("프로필 조회 API")
	@Test
	void checkEmail() throws Exception {
		// given
		given(clientService.getUserProfile())
			.willReturn(new UserProfileResponse("user@email.com", SnsType.NORMAL, Gender.FEMALE, Age.TWENTIES));

		// when // then
		mockMvc.perform(
				get("/user/clients/me")
					.accept(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("client-get-me",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer JWT Access Token")
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("응답 코드"),
					fieldWithPath("data.email").type(JsonFieldType.STRING)
						.description("사용자 이메일"),
					fieldWithPath("data.snsType").type(JsonFieldType.STRING)
						.description("SNS 타입. 가능한 값: " + Arrays.toString(SnsType.values())),
					fieldWithPath("data.gender").type(JsonFieldType.STRING)
						.description("성별. 가능한 값: " + Arrays.toString(Gender.values())),
					fieldWithPath("data.age").type(JsonFieldType.STRING)
						.description("연령대. 가능한 값: " + Arrays.toString(Age.values()))
				)
			));
	}

	@DisplayName("프로필 저장 API")
	@Test
	void updateProfile() throws Exception {
		// given
		UserProfileUpdateRequest request = new UserProfileUpdateRequest(Gender.FEMALE, Age.TWENTIES);

		given(clientService.updateUserProfile(any(UserProfileUpdateServiceRequest.class)))
			.willReturn(UUID.randomUUID());

		// when // then
		mockMvc.perform(
				patch("/user/clients/me")
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("client-patch-me",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer JWT Access Token")
				),
				requestFields(
					fieldWithPath("gender").type(JsonFieldType.STRING)
						.description("성별. 가능한 값: " + Arrays.toString(Gender.values())),
					fieldWithPath("age").type(JsonFieldType.STRING)
						.description("연령대. 가능한 값: " + Arrays.toString(Age.values()))
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("응답 코드"),
					fieldWithPath("data").type(JsonFieldType.STRING)
						.description("응답 데이터, 저장된 사용자 ID")
				)
			));
	}
}

