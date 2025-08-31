package com.seomse.interaction.appointment.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "appointment_detail")
@Entity
public class AppointmentDetailEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id")
	private AppointmentEntity appointment;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private ScaleType scaleType;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private HairType hairType;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private HairLength hairLength;

	@Column(nullable = false, length = 50)
	@Enumerated(EnumType.STRING)
	private HairTreatmentType hairTreatmentType;

	@Column(length = 5000)
	private String requirements;

	@Column(length = 190)
	private String requirementsImage;

	public AppointmentDetailEntity(AppointmentEntity appointment, ScaleType scaleType, HairType hairType,
		HairLength hairLength, HairTreatmentType hairTreatmentType, String requirements,
		String requirementsImage) {
		this.appointment = appointment;
		this.scaleType = scaleType;
		this.hairType = hairType;
		this.hairLength = hairLength;
		this.hairTreatmentType = hairTreatmentType;
		this.requirements = requirements;
		this.requirementsImage = requirementsImage;
	}
}
