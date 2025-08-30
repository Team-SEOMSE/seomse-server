package com.seomse.shop.entity;

import com.seomse.shop.enums.Type;
import com.seomse.user.owner.entity.OwnerEntity;

public final class ShopTestFactory {

	public static ShopEntity newShop(OwnerEntity owner, Type shopType, String shopName,
		String shopInfo, String shopImage) {
		return new ShopEntity(owner, shopType, shopName, shopInfo, shopImage);
	}
}
