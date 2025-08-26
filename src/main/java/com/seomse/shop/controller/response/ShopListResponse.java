package com.seomse.shop.controller.response;

import java.util.UUID;

import com.seomse.shop.enums.Type;

public record ShopListResponse(
	UUID shopId,
	Type shopType,
	String shopName,
	String shopInfo,
	String shopImage
) {
}
