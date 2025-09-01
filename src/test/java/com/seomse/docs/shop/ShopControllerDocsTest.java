package com.seomse.docs.shop;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.seomse.docs.RestDocsSupport;
import com.seomse.shop.controller.ShopController;
import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.dto.DesignerInfoDto;
import com.seomse.shop.service.ShopService;
import com.seomse.shop.service.response.ShopDetailResponse;
import com.seomse.shop.service.response.ShopListResponse;

public class ShopControllerDocsTest extends RestDocsSupport {

	private final ShopService shopService = Mockito.mock(ShopService.class);

	@Override
	protected Object initController() {
		return new ShopController(shopService);
	}

	@DisplayName("shop 전체 조회 API")
	@Test
	void getShopList() throws Exception {
		// given
		List<ShopListResponse> response = List.of(
			new ShopListResponse(UUID.randomUUID(), Type.HAIR_SALON, "shopName1",
				"info1", "/img.png"
			));
		given(shopService.getShopList(Type.HAIR_SALON)).willReturn(response);

		// when // then
		mockMvc.perform(
				get("/shops")
					.param("type", Type.HAIR_SALON.name())
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("shop-get-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("type").description("Shop type (예: HAIR_SALON)")
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("응답 코드"),
					fieldWithPath("data[].shopId").type(JsonFieldType.STRING)
						.description("샵 ID"),
					fieldWithPath("data[].shopType").type(JsonFieldType.STRING)
						.description("샵 종류. 가능한 값: " + Arrays.toString(Type.values())),
					fieldWithPath("data[].shopName").type(JsonFieldType.STRING)
						.description("샵 이름"),
					fieldWithPath("data[].shopInfo").type(JsonFieldType.STRING)
						.description("샵 설명"),
					fieldWithPath("data[].shopImage").type(JsonFieldType.STRING)
						.description("샵 이미지")
				)
			));
	}

	@DisplayName("shop 단일 조회 API")
	@Test
	void getShopDetail() throws Exception {
		// given
		UUID shopId = UUID.randomUUID();
		ShopDetailResponse response = new ShopDetailResponse(
			Type.HAIR_SALON,
			"shopName",
			"shopInfo",
			"/img.png",
			List.of(new DesignerInfoDto(UUID.randomUUID(), "designerNickName"))
		);

		given(shopService.getShopDetail(shopId)).willReturn(response);

		// when // then
		mockMvc.perform(
				get("/shops/{shopId}", shopId)
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(print())
			.andDo(document("shop-get-detail",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					parameterWithName("shopId").description("조회할 샵의 UUID")
				),
				responseFields(
					fieldWithPath("statusCode").type(JsonFieldType.NUMBER)
						.description("응답 코드"),
					fieldWithPath("data.shopType").type(JsonFieldType.STRING)
						.description("샵 종류. 가능한 값: " + Arrays.toString(Type.values())),
					fieldWithPath("data.shopName").type(JsonFieldType.STRING)
						.description("샵 이름"),
					fieldWithPath("data.shopInfo").type(JsonFieldType.STRING)
						.description("샵 위치, 전화번호, 소개글"),
					fieldWithPath("data.shopImage").type(JsonFieldType.STRING)
						.description("샵 이미지"),
					fieldWithPath("data.designers[].designerId").type(JsonFieldType.STRING)
						.description("디자이너 UUID"),
					fieldWithPath("data.designers[].nickName").type(JsonFieldType.STRING)
						.description("디자이너 닉네임")
				)
			));
	}
}
