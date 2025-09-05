package com.seomse.shop.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.seomse.IntegrationTestSupport;
import com.seomse.interaction.appointment.repository.AppointmentRepository;
import com.seomse.interaction.review.repository.ReviewRepository;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.DesignerShopRepository;
import com.seomse.shop.repository.ShopRepository;
import com.seomse.shop.repository.dto.DesignerInfoDto;
import com.seomse.shop.service.response.ShopDetailResponse;
import com.seomse.shop.service.response.ShopListResponse;
import com.seomse.user.designer.entity.DesignerEntity;
import com.seomse.user.designer.repository.DesignerRepository;
import com.seomse.user.owner.entity.OwnerEntity;
import com.seomse.user.owner.repository.OwnerRepository;

import jakarta.persistence.EntityNotFoundException;

class ShopServiceTest extends IntegrationTestSupport {

	@Autowired
	private ShopService shopService;

	@Autowired
	private ShopRepository shopRepository;

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private DesignerRepository designerRepository;

	@Autowired
	private DesignerShopRepository designerShopRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@AfterEach
	void tearDown() {
		reviewRepository.deleteAll();
		appointmentRepository.deleteAll();
		designerShopRepository.deleteAll();
		shopRepository.deleteAll();
		designerRepository.deleteAll();
		ownerRepository.deleteAll();
	}

	@DisplayName("ShopType을 받으면 type에 해당하는 ShopList를 반환한다.")
	@Test
	void getShopList_whenTypeExists_thenReturnList() {
		//given
		// owner1
		String ownerEmail1 = "user1@email.com";
		String ownerPassword1 = "abc1234!";

		OwnerEntity owner1 = new OwnerEntity(ownerEmail1, ownerPassword1);
		ownerRepository.save(owner1);

		// shop1
		Type shopType1 = Type.HAIR_SALON;
		String shopName1 = "shopName1";
		String shopInfo1 = "info1";
		String shopImage1 = "/img1.png";

		ShopEntity shop1 = new ShopEntity(owner1, shopType1, shopName1, shopInfo1, shopImage1);

		shopRepository.save(shop1);

		// owner2
		String ownerEmail2 = "user2@email.com";
		String ownerPassword2 = "cba4321!";

		OwnerEntity owner2 = new OwnerEntity(ownerEmail2, ownerPassword2);

		ownerRepository.save(owner2);

		// shop2
		Type shopType2 = Type.HAIR_SALON;
		String shopName2 = "shopName2";
		String shopInfo2 = "info2";
		String shopImage2 = "/img2.png";

		ShopEntity shop2 = new ShopEntity(owner2, shopType2, shopName2, shopInfo2, shopImage2);

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

	@DisplayName("shopId로 단건 조회 시 shop의 정보와 designer정보를 반환한다.")
	@Test
	void getShopDetail_whenExists_thenReturnShopAndDesigner() {
		//given
		// owner1
		String ownerEmail1 = "user1@email.com";
		String ownerPassword1 = "abc1234!";

		OwnerEntity owner1 = new OwnerEntity(ownerEmail1, ownerPassword1);

		ownerRepository.save(owner1);

		// shop1
		Type shopType1 = Type.HAIR_SALON;
		String shopName1 = "shopName1";
		String shopInfo1 = "info1";
		String shopImage1 = "/img1.png";

		ShopEntity shop1 = new ShopEntity(owner1, shopType1, shopName1, shopInfo1, shopImage1);

		shopRepository.save(shop1);

		// designer10
		String designerEmail10 = "designer10@email.com";
		String designerPassword10 = "designer101234!";
		String designerNickName10 = "designerNickName10";

		DesignerEntity designer10 = new DesignerEntity(designerEmail10, designerPassword10, designerNickName10);

		designerRepository.save(designer10);

		// designer11
		String designerEmail11 = "designer11@email.com";
		String designerPassword11 = "designer111234!";
		String designerNickName11 = "designerNickName11";

		DesignerEntity designer11 = new DesignerEntity(designerEmail11, designerPassword11, designerNickName11);

		designerRepository.save(designer11);

		// designerShop1
		designerShopRepository.save(new DesignerShopEntity(designer10, shop1));
		designerShopRepository.save(new DesignerShopEntity(designer11, shop1));

		//when
		ShopDetailResponse response = shopService.getShopDetail(shop1.getId());

		//then
		// 샵 이름
		assertThat(response.shopName()).isEqualTo("shopName1");

		// 디자이너 데이터 매핑
		assertThat(response.designers()).hasSize(2)
			.extracting(DesignerInfoDto::nickname)
			.containsExactlyInAnyOrder("designerNickName10", "designerNickName11");
	}

	@DisplayName("shopId로 단건 조회 시, designer가 없을 경우 shop의 정보와 빈 designer 리스트를 반환한다.")
	@Test
	void getShopDetail_whenDesignerNotExists_thenReturnEmptyList() {
		// given
		// owner1
		String ownerEmail1 = "user1@email.com";
		String ownerPassword1 = "abc1234!";

		OwnerEntity owner1 = new OwnerEntity(ownerEmail1, ownerPassword1);

		ownerRepository.save(owner1);

		// shop1
		Type shopType1 = Type.HAIR_SALON;
		String shopName1 = "shopName1";
		String shopInfo1 = "info1";
		String shopImage1 = "/img1.png";

		ShopEntity shop1 = new ShopEntity(owner1, shopType1, shopName1, shopInfo1, shopImage1);

		shopRepository.save(shop1);

		// when
		ShopDetailResponse response = shopService.getShopDetail(shop1.getId());

		// then
		assertThat(response.designers()).isEmpty();
	}

	@DisplayName("shopId로 단건 조회 시, shop이 없을 경우 Shop not found")
	@Test
	void getShopDetail_whenShopNotExists_thenReturnShopNotFound() {
		// given
		UUID nonExistId = UUID.randomUUID();

		// when // then
		assertThatThrownBy(() -> shopService.getShopDetail(nonExistId))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessage("Shop not found.");
	}
}
