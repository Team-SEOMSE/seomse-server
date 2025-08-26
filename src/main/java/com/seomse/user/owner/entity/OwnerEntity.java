package com.seomse.user.owner.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

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
public class OwnerEntity {

	@Id
	@UuidGenerator
	@Column(name = "owner_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID ownerId;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = false, length = 60)
	private String password;

	// 테스트 전용: 패키지 전용 생성자 (public 아님)
	OwnerEntity(String email, String password) {
		this.email = email;
		this.password = password;
	}
}
