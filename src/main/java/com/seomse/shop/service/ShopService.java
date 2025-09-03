package com.seomse.shop.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.ShopQueryRepository;
import com.seomse.shop.repository.ShopRepository;
import com.seomse.shop.repository.dto.ShopDesignersDto;
import com.seomse.shop.service.response.ShopDetailResponse;
import com.seomse.shop.service.response.ShopListResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ShopService {

	private final ShopRepository shopRepository;
	private final ShopQueryRepository shopQueryRepository;

	public List<ShopListResponse> getShopList(Type type) {
		List<ShopEntity> shopList = shopRepository.findAllByType(type);
		return shopList.stream()
			.map(shop -> new ShopListResponse(
				shop.getId(),
				shop.getType(),
				shop.getName(),
				shop.getInfo(),
				shop.getImage()
			))
			.toList();
	}

	public ShopDetailResponse getShopDetail(UUID shopId) {
		ShopDesignersDto shopDesigners = shopQueryRepository.findShopDetailByShopId(shopId)
			.orElseThrow(() -> new EntityNotFoundException("Shop not found."));

		return new ShopDetailResponse(
			shopDesigners.shop().getType(),
			shopDesigners.shop().getName(),
			shopDesigners.shop().getInfo(),
			shopDesigners.shop().getImage(),
			shopDesigners.designers()
		);
	}
}
