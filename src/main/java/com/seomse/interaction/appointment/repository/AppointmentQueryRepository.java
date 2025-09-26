package com.seomse.interaction.appointment.repository;

import static com.seomse.interaction.appointment.entity.QAppointmentDetailEntity.*;
import static com.seomse.interaction.appointment.entity.QAppointmentEntity.*;
import static com.seomse.shop.entity.QDesignerShopEntity.*;
import static com.seomse.shop.entity.QShopEntity.*;
import static com.seomse.user.designer.entity.QDesignerEntity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seomse.interaction.appointment.service.response.AppointmentDetailResponse;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;
import com.seomse.interaction.appointment.service.response.AppointmentTimeListResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class AppointmentQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	public List<AppointmentListResponse> findAppointmentListByClientId(UUID clientId) {
		return jpaQueryFactory
			.select(Projections.constructor(AppointmentListResponse.class,
				appointmentEntity.id,
				shopEntity.name,
				designerEntity.nickname,
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
				shopEntity.name,
				designerEntity.nickname,
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
				shopEntity.name,
				designerEntity.nickname,
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

	public Optional<AppointmentDetailResponse> findAppointmentDetail(UUID appointmentId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.select(Projections.constructor(AppointmentDetailResponse.class,
					appointmentDetailEntity.scaleType,
					appointmentDetailEntity.hairType,
					appointmentDetailEntity.hairLength,
					appointmentDetailEntity.hairTreatmentType,
					appointmentDetailEntity.requirements,
					appointmentDetailEntity.requirementsImage
				))
				.from(appointmentDetailEntity)
				.join(appointmentDetailEntity.appointment, appointmentEntity)
				.where(appointmentDetailEntity.appointment.id.eq(appointmentId))
				.fetchOne()
		);
	}

	public Optional<AppointmentDetailResponse> findAppointmentDetailByLatest(UUID clientId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.select(Projections.constructor(AppointmentDetailResponse.class,
					appointmentDetailEntity.scaleType,
					appointmentDetailEntity.hairType,
					appointmentDetailEntity.hairLength,
					appointmentDetailEntity.hairTreatmentType,
					appointmentDetailEntity.requirements,
					appointmentDetailEntity.requirementsImage
				))
				.from(appointmentDetailEntity)
				.join(appointmentDetailEntity.appointment, appointmentEntity)
				.where(appointmentEntity.client.id.eq(clientId))
				.orderBy(appointmentEntity.createdDate.desc())
				.limit(1)
				.fetchOne()
		);
	}

	public List<AppointmentTimeListResponse> findAppointmentListByToday(UUID shopId, UUID designer,
		LocalDate appointmentDate, LocalTime now) {
		return jpaQueryFactory
			.select(Projections.constructor(AppointmentTimeListResponse.class,
				appointmentEntity.appointmentTime
			))
			.from(appointmentEntity)
			.where(appointmentEntity.designerShop.shop.id.eq(shopId)
				.and(appointmentEntity.designerShop.designer.id.eq(designer))
				.and(appointmentEntity.appointmentDate.eq(appointmentDate)
					.and(appointmentEntity.appointmentTime.gt(now))
				)
			)
			.fetch();
	}

	public List<AppointmentTimeListResponse> findAppointmentListByDate(UUID shopId, UUID designer,
		LocalDate appointmentDate) {
		return jpaQueryFactory
			.select(Projections.constructor(AppointmentTimeListResponse.class,
				appointmentEntity.appointmentTime
			))
			.from(appointmentEntity)
			.where(appointmentEntity.designerShop.shop.id.eq(shopId)
				.and(appointmentEntity.designerShop.designer.id.eq(designer))
				.and(appointmentEntity.appointmentDate.eq(appointmentDate))
			)
			.fetch();
	}
}
