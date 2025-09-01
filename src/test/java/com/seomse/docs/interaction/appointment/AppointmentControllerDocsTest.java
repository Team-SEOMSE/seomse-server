package com.seomse.docs.interaction.appointment;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;

import com.seomse.docs.RestDocsSupport;
import com.seomse.interaction.appointment.controller.AppointmentController;
import com.seomse.interaction.appointment.controller.request.AppointmentCreateRequest;
import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;
import com.seomse.interaction.appointment.service.AppointmentService;
import com.seomse.interaction.appointment.service.response.AppointmentDetailResponse;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;

public class AppointmentControllerDocsTest extends RestDocsSupport {

	private final AppointmentService appointmentService = Mockito.mock(AppointmentService.class);

	@Override
	protected Object initController() {
		return new AppointmentController(appointmentService);
	}

	@DisplayName("예약 생성 API")
	@Test
	void updateProfile() throws Exception {
		// given
		UUID appointmentId = UUID.randomUUID();
		given(appointmentService.createAppointment(any(AppointmentCreateRequest.class), any()))
			.willReturn(appointmentId);

		// request
		AppointmentCreateRequest request = new AppointmentCreateRequest(
			UUID.randomUUID(), // shopId
			UUID.randomUUID(), // designerId
			"커트",
			ScaleType.NEUTRAL,
			HairType.CURLY,
			HairLength.SHORT_CUT,
			HairTreatmentType.BLEACH,
			"requirements"
		);

		String requestJson = objectMapper.writeValueAsString(request);

		// requestPart
		MockMultipartFile requestPart = new MockMultipartFile(
			"request",
			"request.json",
			"application/json",
			requestJson.getBytes()
		);
		MockMultipartFile imagePart = new MockMultipartFile(
			"requirementsImage", "test.png", "image/png", "test image".getBytes()
		);

		// when // then
		mockMvc.perform(
				multipart("/interaction/appointments")
					.file(requestPart)
					.file(imagePart)
					.content(objectMapper.writeValueAsString(request))
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
			)
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("appointment-create",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParts(
					partWithName("request").description("예약 생성 요청 JSON"),
					partWithName("requirementsImage").description("요구사항 이미지 (optional)").optional()
				),
				requestPartFields("request",
					fieldWithPath("shopId").description("샵 UUID"),
					fieldWithPath("designerId").description("디자이너 UUID"),
					fieldWithPath("serviceName").description("시술명"),
					fieldWithPath("scaleType").description("두피 타입. 가능한 값: " + Arrays.toString(ScaleType.values())),
					fieldWithPath("hairType").description("모발 타입. 가능한 값: " + Arrays.toString(HairType.values())),
					fieldWithPath("hairLength").description("머리 길이. 가능한 값: " + Arrays.toString(HairLength.values())),
					fieldWithPath("hairTreatmentType").description(
						"모발 시술 이력. 가능한 값: " + Arrays.toString(HairTreatmentType.values())),
					fieldWithPath("requirements").description("추가 요청사항").optional()
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("응답 코드"),
					fieldWithPath("data").type(JsonFieldType.STRING).description("생성된 예약 UUID")
				)
			));
	}

	@DisplayName("예약 조회(전체) API")
	@Test
	void getAppointmentList() throws Exception {
		// given
		List<AppointmentListResponse> response = List.of(new AppointmentListResponse(
				UUID.randomUUID(), "shopName1", "designerNickName1",
				"serviceName1", LocalDateTime.of(2025, 12, 25, 12, 0)),
			new AppointmentListResponse(
				UUID.randomUUID(), "shopName2", "designerNickName1",
				"serviceName2", LocalDateTime.of(2025, 12, 25, 13, 0))
		);
		given(appointmentService.getAppointmentList()).willReturn(response);

		// when // then
		mockMvc.perform(
				get("/interaction/appointments")
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("appointment-get-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("응답 코드"),
					fieldWithPath("data[].appointmentId").type(JsonFieldType.STRING).description("예약 UUID"),
					fieldWithPath("data[].shopName").type(JsonFieldType.STRING).description("샵 이름"),
					fieldWithPath("data[].designerNickname").type(JsonFieldType.STRING).description("디자이너 닉네임"),
					fieldWithPath("data[].serviceName").type(JsonFieldType.STRING).description("시술명"),
					fieldWithPath("data[].appointmentDate").type(JsonFieldType.STRING)
						.description("예약 일시 (yyyy-MM-dd HH:mm:ss)")
				)
			));
	}

	@DisplayName("예약 세부사항 조회(단일) API")
	@Test
	void getAppointment() throws Exception {
		// given
		UUID appointmentId = UUID.randomUUID();
		AppointmentDetailResponse response = new AppointmentDetailResponse(
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"requirements",
			"/requirements/image.png"
		);
		given(appointmentService.getAppointment(any(UUID.class))).willReturn(response);

		// when // then
		mockMvc.perform(
				get("/interaction/appointments/{appointmentId}/details", appointmentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("appointment-get-detail",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("appointmentId").description("조회할 예약의 UUID")
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("응답 코드"),
					fieldWithPath("data.scaleType").description("두피 타입"),
					fieldWithPath("data.hairType").description("모발 타입"),
					fieldWithPath("data.hairLength").description("머리 길이"),
					fieldWithPath("data.hairTreatmentType").description("모발 시술 이력"),
					fieldWithPath("data.requirements").type(JsonFieldType.STRING).description("고객 요청사항"),
					fieldWithPath("data.requirementsImage").type(JsonFieldType.STRING).description("요청사항 이미지")
				)
			));
	}

	@DisplayName("예약 세부사항 조회(단일 최신) API")
	@Test
	void getAppointmentByLatest() throws Exception {
		// given
		AppointmentDetailResponse response = new AppointmentDetailResponse(
			ScaleType.NEUTRAL,
			HairType.CURLY,
			HairLength.SHORT_CUT,
			HairTreatmentType.BLEACH,
			"requirements",
			"/requirements/image.png"
		);

		given(appointmentService.getAppointmentByLatest()).willReturn(response);

		// when // then
		mockMvc.perform(
				get("/interaction/appointments/details")
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("appointment-get-detail-latest",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer <JWT ACCESS TOKEN>")
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("응답 코드"),
					fieldWithPath("data.scaleType").description("두피 타입"),
					fieldWithPath("data.hairType").description("모발 타입"),
					fieldWithPath("data.hairLength").description("머리 길이"),
					fieldWithPath("data.hairTreatmentType").description("모발 시술 이력"),
					fieldWithPath("data.requirements").type(JsonFieldType.STRING).description("고객 요청사항"),
					fieldWithPath("data.requirementsImage").type(JsonFieldType.STRING).description("요청사항 이미지")
				)
			));
	}

}
