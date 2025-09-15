package com.seomse.fixture.shop;

import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;
import com.seomse.user.owner.entity.OwnerEntity;

public class ShopFixture {
	public static ShopEntity createShopEntity(OwnerEntity owner) {
		return new ShopEntity(owner, Type.HAIR_SALON, "shopName", "info", "/img1.png");
	}
}
