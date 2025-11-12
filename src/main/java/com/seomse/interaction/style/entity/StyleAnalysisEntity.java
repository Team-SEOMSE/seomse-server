package com.seomse.interaction.style.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;
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
@Table(name = "style_analysis")
@Entity
public class StyleAnalysisEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private ClientEntity client;

	@Column(length = 190, nullable = false)
	private String image;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false)
	private StyleAnalysisResponse result;

	public StyleAnalysisEntity(ClientEntity client, String image, StyleAnalysisResponse result) {
		this.client = client;
		this.image = image;
		this.result = result;
	}
}

