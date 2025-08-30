package com.seomse.user.owner.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.seomse.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "owner")
@Entity
public class OwnerEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "owner_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID ownerId;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = false, length = 60)
	private String password;

	public OwnerEntity(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
