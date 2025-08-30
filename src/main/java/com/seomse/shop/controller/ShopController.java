package com.seomse.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seomse.common.controller.ApiResponse;
import com.seomse.shop.enums.Type;
import com.seomse.shop.service.ShopService;
import com.seomse.shop.service.response.ShopListResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/shops")
@RestController
public class ShopController {

	private final ShopService shopService;

	@GetMapping
	public ApiResponse<List<ShopListResponse>> getShopList(
		@RequestParam(required = true) Type type) {
		return ApiResponse.ok(shopService.getShopList(type));
	}
}
