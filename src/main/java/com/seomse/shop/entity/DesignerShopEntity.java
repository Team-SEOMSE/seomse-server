package com.seomse.shop.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.seomse.common.entity.BaseTimeEntity;
import com.seomse.user.designer.entity.DesignerEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "designer_shop",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"designer_id", "shop_id"})
	}
)
@Entity
public class DesignerShopEntity extends BaseTimeEntity {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "designer_id")
	private DesignerEntity designer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id")
	private ShopEntity shop;

	public DesignerShopEntity(DesignerEntity designer, ShopEntity shop) {
		this.designer = designer;
		this.shop = shop;
	}
}
