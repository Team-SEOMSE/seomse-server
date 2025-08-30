package com.seomse.shop.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seomse.designerShop.entity.QDesignerShopEntity;
import com.seomse.shop.entity.QShopEntity;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.service.response.ShopDetailResponse;
import com.seomse.user.designer.entity.QDesignerEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ShopQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<ShopDetailResponse> findShopDetailByShopIdWithLeftJoin(UUID shopId) {
		List<Tuple> results = jpaQueryFactory
			.select(QShopEntity.shopEntity, QDesignerEntity.designerEntity)
			.from(QShopEntity.shopEntity)
			.leftJoin(QDesignerShopEntity.designerShopEntity)
			.on(QDesignerShopEntity.designerShopEntity.shop.eq(QShopEntity.shopEntity))
			.leftJoin(QDesignerShopEntity.designerShopEntity.designer,
				QDesignerEntity.designerEntity)
			.where(QShopEntity.shopEntity.shopId.eq(shopId))
			.fetch();

		if (results.isEmpty()) {
			return Optional.empty();
		}

		ShopEntity shop = results.get(0).get(QShopEntity.shopEntity);

		List<ShopDetailResponse.DesignerResponse> designerResponses = results.stream()
			.map(tuple -> tuple.get(QDesignerEntity.designerEntity))
			.filter(Objects::nonNull)
			.map(designer -> new ShopDetailResponse.DesignerResponse(
				designer.getDesignerId(),
				designer.getNickName()
			))
			.toList();

		return Optional.of(new ShopDetailResponse(
			shop.getShopType(),
			shop.getShopName(),
			shop.getShopInfo(),
			shop.getShopImage(),
			designerResponses
		));
	}
}
