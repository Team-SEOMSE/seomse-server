package com.seomse.interaction.review.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.interaction.appointment.entity.AppointmentEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "review")
@Entity
public class ReviewEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false)
	private AppointmentEntity appointment;

	@Column(nullable = false, length = 1)
	private String rating;

	@Column(nullable = false, length = 5000)
	private String content;

	@Column(length = 190)
	private String image;

	public ReviewEntity(AppointmentEntity appointment, String rating, String content, String image) {
		this.appointment = appointment;
		this.rating = rating;
		this.content = content;
		this.image = image;
	}
}
