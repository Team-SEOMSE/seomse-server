package com.seomse.interaction.review.repository;

import static com.seomse.interaction.appointment.entity.QAppointmentEntity.*;
import static com.seomse.interaction.review.entity.QReviewEntity.*;
import static com.seomse.shop.entity.QDesignerShopEntity.*;
import static com.seomse.shop.entity.QShopEntity.*;
import static com.seomse.user.designer.entity.QDesignerEntity.*;
import static com.seomse.user.owner.entity.QOwnerEntity.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seomse.interaction.review.service.response.ReviewListResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ReviewQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	public List<ReviewListResponse> findReviewListByDesigner(UUID designerId) {
		return jpaQueryFactory
			.select(Projections.constructor(
				ReviewListResponse.class,
				reviewEntity.id,
				reviewEntity.rating,
				reviewEntity.content,
				reviewEntity.image,
				shopEntity.name,
				designerEntity.nickname,
				appointmentEntity.serviceName,
				appointmentEntity.createdDate
			))
			.from(reviewEntity)
			.join(reviewEntity.appointment, appointmentEntity)
			.join(appointmentEntity.designerShop, designerShopEntity)
			.join(designerShopEntity.shop, shopEntity)
			.join(designerShopEntity.designer, designerEntity)
			.where(designerEntity.id.eq(designerId))
			.fetch();
	}

	public List<ReviewListResponse> findReviewListByOwner(UUID ownerId) {
		return jpaQueryFactory
			.select(Projections.constructor(
				ReviewListResponse.class,
				reviewEntity.id,
				reviewEntity.rating,
				reviewEntity.content,
				reviewEntity.image,
				shopEntity.name,
				designerEntity.nickname,
				appointmentEntity.serviceName,
				appointmentEntity.createdDate
			))
			.from(reviewEntity)
			.join(reviewEntity.appointment, appointmentEntity)
			.join(appointmentEntity.designerShop, designerShopEntity)
			.join(designerShopEntity.shop, shopEntity)
			.join(designerShopEntity.designer, designerEntity)
			.join(shopEntity.owner, ownerEntity)
			.where(ownerEntity.id.eq(ownerId))
			.fetch();
	}

}
