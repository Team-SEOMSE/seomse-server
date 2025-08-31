package com.seomse.user.client.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

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
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = true, length = 60)
	private String password;

	@Column(nullable = false, length = 60)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SnsType snsType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 20)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 20)
	private Age age;

	public ClientEntity(String email, String encodedPassword, String name, SnsType snsType, Gender gender, Age age) {
		this.email = email;
		this.password = encodedPassword;
		this.name = name;
		this.snsType = snsType;
		this.gender = gender;
		this.age = age;
	}

	public ClientEntity updateProfile(Gender gender, Age age) {
		this.gender = gender;
		this.age = age;

		return this;
	}
}
