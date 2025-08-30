package com.seomse.shop.service.response;

import java.util.List;

import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.dto.DesignerInfoDto;

public record ShopDetailResponse(
	Type shopType,
	String shopName,
	String shopInfo,
	String shopImage,
	List<DesignerInfoDto> designers
) {
}
