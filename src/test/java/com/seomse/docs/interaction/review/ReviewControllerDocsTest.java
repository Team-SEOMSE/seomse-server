package com.seomse.docs.interaction.review;

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
import com.seomse.interaction.review.controller.ReviewController;
import com.seomse.interaction.review.controller.request.ReviewCreateRequest;
import com.seomse.interaction.review.service.ReviewService;
import com.seomse.interaction.review.service.response.ReviewListResponse;

public class ReviewControllerDocsTest extends RestDocsSupport {

	private final ReviewService reviewService = Mockito.mock(ReviewService.class);

	@Override
	protected Object initController() {
		return new ReviewController(reviewService);
	}

	@DisplayName("리뷰 작성 API")
	@Test
	void createReview() throws Exception {
		// given
		// request
		ReviewCreateRequest request = new ReviewCreateRequest(
			UUID.randomUUID(),
			"5",
			"reviewContent"
		);

		// requestPart
		String requestJson = objectMapper.writeValueAsString(request);

		MockMultipartFile requestPart = new MockMultipartFile(
			"request", null, "application/json", requestJson.getBytes()
		);
		MockMultipartFile imagePart = new MockMultipartFile(
			"reviewImage", "image.png", "image/png", "image data".getBytes()
		);

		given(reviewService.createReview(any(), any()))
			.willReturn(UUID.randomUUID());

		// when // then
		mockMvc.perform(
				multipart("/interaction/reviews")
					.file(requestPart)
					.file(imagePart)
					.header(HttpHeaders.AUTHORIZATION, "Bearer JWT ACCESS TOKEN")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isCreated())
			.andDo(document("review-create",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				requestParts(
					partWithName("request").description("리뷰 작성 요청 JSON"),
					partWithName("reviewImage").description("리뷰 이미지 (optional)").optional()
				),
				requestPartFields("request",
					fieldWithPath("appointmentId").type(JsonFieldType.STRING).description("예약 UUID"),
					fieldWithPath("reviewRating").type(JsonFieldType.STRING).description("리뷰 평점 (1~5)"),
					fieldWithPath("reviewContent").type(JsonFieldType.STRING).description("리뷰 내용")
				),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer JWT Access Token")
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("응답 코드"),
					fieldWithPath("data").type(JsonFieldType.STRING).description("생성된 리뷰 ID")
				)
			));
	}

	@DisplayName("디자이너, 점주 리뷰 조회(전체) API")
	@Test
	void getReviewList() throws Exception {
		// given
		List<ReviewListResponse> response = List.of(
			new ReviewListResponse(
				UUID.randomUUID(),
				"5",
				"requireContent1",
				"/review/image1.png",
				"shopName1",
				"designerNickName1",
				"serviceName1",
				LocalDateTime.of(2025, 12, 25, 12, 0, 0)
			),
			new ReviewListResponse(
				UUID.randomUUID(),
				"5",
				"requireContent2",
				"/review/image2.png",
				"shopName2",
				"designerNickName2",
				"serviceName2",
				LocalDateTime.of(2025, 12, 25, 13, 0, 0)
			)
		);

		given(reviewService.getReviewList()).willReturn(response);

		// when // then
		mockMvc.perform(
				get("/interaction/reviews")
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("review-get-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("응답 코드"),
					fieldWithPath("data[].reviewId").type(JsonFieldType.STRING)
						.description("리뷰 UUID"),
					fieldWithPath("data[].reviewRating").type(JsonFieldType.STRING)
						.description("리뷰 평점 (1~5)"),
					fieldWithPath("data[].reviewContent").type(JsonFieldType.STRING)
						.description("리뷰 내용"),
					fieldWithPath("data[].reviewImage").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 이미지 (선택)"),
					fieldWithPath("data[].shopName").type(JsonFieldType.STRING)
						.description("샵 이름"),
					fieldWithPath("data[].designerNickName").type(JsonFieldType.STRING)
						.description("디자이너 닉네임"),
					fieldWithPath("data[].serviceName").type(JsonFieldType.STRING)
						.description("시술명"),
					fieldWithPath("data[].appointmentDate").type(JsonFieldType.STRING)
						.description("예약 일시 (yyyy-MM-dd HH:mm:ss)")
				)
			));
	}
}
