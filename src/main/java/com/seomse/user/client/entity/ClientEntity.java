package com.seomse.user.client.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "client")
@Entity
public class ClientEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	private String email;

	private String password;

	@Enumerated(EnumType.STRING)
	private SnsType snsType;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	private Age age;

	// 테스트 전용: 패키지 전용 생성자 (public 아님)
	ClientEntity(String email, String encodedPassword, SnsType snsType, Gender gender, Age age) {
		this.email = email;
		this.password = encodedPassword;
		this.snsType = snsType;
		this.gender = gender;
		this.age = age;
	}
}
