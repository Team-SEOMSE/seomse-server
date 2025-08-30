package com.seomse.shop.service;

import static com.seomse.shop.entity.ShopTestFactory.*;
import static com.seomse.user.owner.entity.OwnerTestFactory.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.seomse.IntegrationTestSupport;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.ShopRepository;
import com.seomse.shop.service.response.ShopListResponse;
import com.seomse.user.owner.entity.OwnerEntity;
import com.seomse.user.owner.repository.OwnerRepository;

class ShopServiceTest extends IntegrationTestSupport {

	@Autowired
	private ShopService shopService;

	@Autowired
	private ShopRepository shopRepository;

	@Autowired
	private OwnerRepository ownerRepository;

	@AfterEach
	void tearDown() {
		shopRepository.deleteAll();
	}

	@DisplayName("ShopType을 받으면 type에 해당하는 ShopList를 반환한다.")
	@Test
	void getShopList_whenTypeExists_thenReturnList() {
		//given
		// owner1
		String ownerEmail1 = "user1@email.com";
		String ownerPassword1 = "abc1234!";

		OwnerEntity owner1 = newOwner(ownerEmail1, ownerPassword1);

		ownerRepository.save(owner1);

		// shop1
		Type shopType1 = Type.HAIR_SALON;
		String shopName1 = "shopName1";
		String shopInfo1 = "info1";
		String shopImage1 = "/img1.png";

		ShopEntity shop1 = newShop(owner1, shopType1, shopName1, shopInfo1, shopImage1);

		shopRepository.save(shop1);

		// owner2
		String ownerEmail2 = "user2@email.com";
		String ownerPassword2 = "cba4321!";

		OwnerEntity owner2 = newOwner(ownerEmail2, ownerPassword2);

		ownerRepository.save(owner2);

		// shop2
		Type shopType2 = Type.HAIR_SALON;
		String shopName2 = "shopName2";
		String shopInfo2 = "info2";
		String shopImage2 = "/img2.png";

		ShopEntity shop2 = newShop(owner2, shopType2, shopName2, shopInfo2, shopImage2);

		shopRepository.save(shop2);

		Type requestType = Type.HAIR_SALON;

		//when
		List<ShopListResponse> shopList = shopService.getShopList(requestType);

		//then
		// 개수
		assertThat(shopList).hasSize(2);

		// 조회 조건 type
		assertThat(shopList).allMatch(
			shop -> shop.shopType().equals(Type.HAIR_SALON));

		// 데이터 매핑
		assertThat(shopList).extracting(ShopListResponse::shopName)
			.contains("shopName1", "shopName2");
	}

	@DisplayName("ShopType을 받고 shopType에 해당하는 ShopList가 없을 경우 빈 리스트를 반환한다.")
	@Test
	void getShopList_whenNotExists_thenReturnEmptyList() {
		//given

		//when
		List<ShopListResponse> emptyList = shopService.getShopList(Type.HAIR_SALON);

		//then
		assertThat(emptyList).isEmpty();
	}
}
