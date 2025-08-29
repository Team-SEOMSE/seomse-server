package com.seomse.shop.service.response;

import java.util.List;
import java.util.UUID;

import com.seomse.shop.enums.Type;

public record ShopDetailResponse(
	Type shopType,
	String shopName,
	String shopInfo,
	String shopImage,
	List<DesignerResponse> designers
) {
	public record DesignerResponse(
		UUID designerId,
		String nickname
	) {
	}
}
