package com.seomse.interaction.appointment.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.user.client.entity.ClientEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "appointment")
@Entity
public class AppointmentEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private ClientEntity client;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "designer_shop_id", nullable = false)
	private DesignerShopEntity designerShop;

	@Column(nullable = false)
	private LocalDate appointmentDate;

	@Column(nullable = false)
	private LocalTime appointmentTime;

	@Column(length = 190, nullable = false)
	private String serviceName;

	public AppointmentEntity(ClientEntity client, DesignerShopEntity designerShop,
		LocalDate appointmentDate, LocalTime appointmentTime, String serviceName) {
		this.client = client;
		this.designerShop = designerShop;
		this.appointmentDate = appointmentDate;
		this.appointmentTime = appointmentTime;
		this.serviceName = serviceName;
	}
}
