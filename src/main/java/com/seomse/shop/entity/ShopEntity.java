package com.seomse.shop.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.shop.enums.Type;
import com.seomse.user.owner.entity.OwnerEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "shop")
@Entity
public class ShopEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private OwnerEntity owner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Type shopType;

	@Column(nullable = false, length = 50)
	private String shopName;

	@Column(length = 100)
	private String shopInfo;

	@Column(length = 190)
	private String shopImage;

	public ShopEntity(OwnerEntity owner, Type shopType, String shopName,
		String shopInfo, String shopImage) {
		this.owner = owner;
		this.shopType = shopType;
		this.shopName = shopName;
		this.shopInfo = shopInfo;
		this.shopImage = shopImage;
	}
}
