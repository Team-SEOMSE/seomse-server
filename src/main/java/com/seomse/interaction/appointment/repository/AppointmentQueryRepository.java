package com.seomse.interaction.appointment.repository;

import static com.seomse.interaction.appointment.entity.QAppointmentEntity.*;
import static com.seomse.shop.entity.QDesignerShopEntity.*;
import static com.seomse.shop.entity.QShopEntity.*;
import static com.seomse.user.designer.entity.QDesignerEntity.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class AppointmentQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	public List<AppointmentListResponse> findAppointmentListByClientId(UUID clientId) {
		return jpaQueryFactory
			.select(Projections.constructor(AppointmentListResponse.class,
				appointmentEntity.id,
				shopEntity.shopName,
				designerEntity.nickName,
				appointmentEntity.serviceName,
				appointmentEntity.createdDate
			))
			.from(appointmentEntity)
			.join(appointmentEntity.designerShop, designerShopEntity)
			.join(designerShopEntity.shop, shopEntity)
			.join(designerShopEntity.designer, designerEntity)
			.where(appointmentEntity.client.id.eq(clientId))
			.fetch();
	}

	public List<AppointmentListResponse> findAppointmentListByOwnerId(UUID ownerId) {
		return jpaQueryFactory
			.select(Projections.constructor(AppointmentListResponse.class,
				appointmentEntity.id,
				shopEntity.shopName,
				designerEntity.nickName,
				appointmentEntity.serviceName,
				appointmentEntity.createdDate
			))
			.from(appointmentEntity)
			.join(appointmentEntity.designerShop, designerShopEntity)
			.join(designerShopEntity.shop, shopEntity)
			.join(designerShopEntity.designer, designerEntity)
			.where(shopEntity.owner.id.eq(ownerId))
			.fetch();
	}

	public List<AppointmentListResponse> findAppointmentListByDesignerId(UUID designerId) {
		return jpaQueryFactory
			.select(Projections.constructor(AppointmentListResponse.class,
				appointmentEntity.id,
				shopEntity.shopName,
				designerEntity.nickName,
				appointmentEntity.serviceName,
				appointmentEntity.createdDate
			))
			.from(appointmentEntity)
			.join(appointmentEntity.designerShop, designerShopEntity)
			.join(designerShopEntity.shop, shopEntity)
			.join(designerShopEntity.designer, designerEntity)
			.where(designerShopEntity.designer.id.eq(designerId))
			.fetch();
	}
}
