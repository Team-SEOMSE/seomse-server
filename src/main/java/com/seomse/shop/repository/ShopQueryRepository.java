package com.seomse.shop.repository;

import static com.seomse.shop.entity.QDesignerShopEntity.*;
import static com.seomse.shop.entity.QShopEntity.*;
import static com.seomse.user.designer.entity.QDesignerEntity.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.repository.dto.DesignerInfoDto;
import com.seomse.shop.repository.dto.ShopDesignersDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ShopQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<ShopDesignersDto> findShopDetailByShopId(UUID shopId) {
		Optional<ShopEntity> optionalShop = Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(shopEntity)
				.where(shopEntity.id.eq(shopId))
				.fetchOne()
		);

		if (optionalShop.isEmpty()) {
			return Optional.empty();
		}

		ShopEntity shop = optionalShop.get();

		List<DesignerInfoDto> designerInfos = jpaQueryFactory
			.select(Projections.constructor(DesignerInfoDto.class,
				designerEntity.id,
				designerEntity.nickname
			))
			.from(designerShopEntity)
			.join(designerShopEntity.designer, designerEntity)
			.where(designerShopEntity.shop.id.eq(shop.getId()))
			.fetch();

		return Optional.of(new ShopDesignersDto(shop, designerInfos));
	}
}
