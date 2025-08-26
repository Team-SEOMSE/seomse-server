package com.seomse.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seomse.shop.controller.response.ShopListResponse;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ShopService {

	private final ShopRepository shopRepository;

	public List<ShopListResponse> getShopList(Type type) {
		List<ShopEntity> shopList = shopRepository.findAllByShopType(type);
		return shopList.stream()
			.map(shop -> new ShopListResponse(
				shop.getShopId(),
				shop.getShopType(),
				shop.getShopName(),
				shop.getShopInfo(),
				shop.getShopImage()
			))
			.toList();
	}
}
