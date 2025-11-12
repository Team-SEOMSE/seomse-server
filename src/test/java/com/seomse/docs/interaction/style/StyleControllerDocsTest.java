package com.seomse.docs.interaction.style;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.docs.RestDocsSupport;
import com.seomse.interaction.style.controller.StyleController;
import com.seomse.interaction.style.service.StyleService;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;

public class StyleControllerDocsTest extends RestDocsSupport {

	private final StyleService styleService = Mockito.mock(StyleService.class);

	@Override
	protected Object initController() {
		return new StyleController(styleService);
	}

	@DisplayName("스타일 분석 API")
	@Test
	void analyzeStyle() throws Exception {
		// given
		StyleAnalysisResponse response = new StyleAnalysisResponse(
			new StyleAnalysisResponse.AnalysisData("계란형", "가을 웜톤"),
			new StyleAnalysisResponse.RecommendationsData(
				new StyleAnalysisResponse.RecommendationDetail("댄디컷", "얼굴형에 잘 어울리며 부드러운 이미지를 연출합니다."),
				new StyleAnalysisResponse.RecommendationDetail("애쉬 브라운", "가을 웜톤 피부에 자연스럽게 어울립니다.")
			)
		);

		given(styleService.callAnalyzeStyle(any(MultipartFile.class)))
			.willReturn(response);

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"test-image.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test-image-content".getBytes()
		);

		// when // then
		mockMvc.perform(
				multipart("/interaction/styles/analysis")
					.file(image)
					.header(HttpHeaders.AUTHORIZATION, "Bearer <JWT ACCESS TOKEN>")
					.contentType(MediaType.MULTIPART_FORM_DATA)
			)
			.andExpect(status().isCreated())
			.andDo(print())
			.andDo(document("style-analysis",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),

				requestParts(
					partWithName("image").description("분석할 사용자 이미지 파일 (jpeg, png 등)")
				),

				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("응답 코드"),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("스타일 분석 결과"),

					fieldWithPath("data.analysis").type(JsonFieldType.OBJECT).description("얼굴 및 퍼스널 컬러 분석"),
					fieldWithPath("data.analysis.faceShape").type(JsonFieldType.STRING).description("분석된 얼굴형"),
					fieldWithPath("data.analysis.personalColor").type(JsonFieldType.STRING).description("분석된 퍼스널 컬러"),

					fieldWithPath("data.recommendations").type(JsonFieldType.OBJECT).description("헤어스타일 추천"),
					fieldWithPath("data.recommendations.hairstyle").type(JsonFieldType.OBJECT).description("추천 헤어스타일"),
					fieldWithPath("data.recommendations.hairstyle.name").type(JsonFieldType.STRING)
						.description("추천 헤어스타일 이름"),
					fieldWithPath("data.recommendations.hairstyle.reason").type(JsonFieldType.STRING)
						.description("추천 이유"),

					fieldWithPath("data.recommendations.hairColor").type(JsonFieldType.OBJECT).description("추천 헤어 컬러"),
					fieldWithPath("data.recommendations.hairColor.name").type(JsonFieldType.STRING)
						.description("추천 헤어 컬러 이름"),
					fieldWithPath("data.recommendations.hairColor.reason").type(JsonFieldType.STRING)
						.description("추천 이유")
				)
			));
	}
}
