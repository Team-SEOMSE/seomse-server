package com.seomse.user.designer.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "designer")
@Entity
public class DesignerEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@Column(name = "designer_id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID designerId;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = false, length = 60)
	private String password;

	@Column(nullable = false, length = 60)
	private String nickName;

	public DesignerEntity(String email, String password, String nickName) {
		this.email = email;
		this.password = password;
		this.nickName = nickName;
	}
}
