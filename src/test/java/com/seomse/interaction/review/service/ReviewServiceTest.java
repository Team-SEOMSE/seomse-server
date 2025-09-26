package com.seomse.interaction.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.seomse.IntegrationTestSupport;
import com.seomse.fixture.interaction.appointment.AppointmentFixture;
import com.seomse.fixture.interaction.review.ReviewFixture;
import com.seomse.fixture.shop.ShopFixture;
import com.seomse.fixture.user.client.ClientFixture;
import com.seomse.fixture.user.designer.DesignerFixture;
import com.seomse.fixture.user.owner.OwnerFixture;
import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.appointment.repository.AppointmentRepository;
import com.seomse.interaction.review.controller.request.ReviewCreateRequest;
import com.seomse.interaction.review.entity.ReviewEntity;
import com.seomse.interaction.review.repository.ReviewRepository;
import com.seomse.interaction.review.service.response.ReviewListResponse;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.repository.DesignerShopRepository;
import com.seomse.shop.repository.ShopRepository;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.repository.ClientRepository;
import com.seomse.user.designer.entity.DesignerEntity;
import com.seomse.user.designer.repository.DesignerRepository;
import com.seomse.user.owner.entity.OwnerEntity;
import com.seomse.user.owner.repository.OwnerRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class ReviewServiceTest extends IntegrationTestSupport {

	@Autowired
	private Clock clock;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private ShopRepository shopRepository;

	@Autowired
	private DesignerRepository designerRepository;

	@Autowired
	private DesignerShopRepository designerShopRepository;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@AfterEach
	void tearDown() {
		reviewRepository.deleteAll();
		appointmentRepository.deleteAll();
		designerShopRepository.deleteAll();
		designerRepository.deleteAll();
		shopRepository.deleteAll();
		ownerRepository.deleteAll();
		clientRepository.deleteAll();
	}

	@DisplayName("리뷰 생성 시, appointmentId로 리뷰가 저장되고 reviewId를 반환한다. (이미지 없음)")
	@Test
	void givenValidRequestWithoutImage_whenCreateReview_thenSaveReviewAndReturnId() {
		//given
		// owner
		OwnerEntity owner = OwnerFixture.createOwnerEntity();
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = ShopFixture.createShopEntity(owner);
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = DesignerFixture.createDesignerEntity();
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		AppointmentEntity appointment = AppointmentFixture.createAppointmentEntity(client, designerShop, clock);
		appointmentRepository.save(appointment);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		String reviewRating = "5";
		String reviewContent = "reviewContent";

		ReviewCreateRequest request = new ReviewCreateRequest(appointment.getId(), reviewRating, reviewContent);

		//when
		UUID reviewId = reviewService.createReview(request, null);

		//then
		ReviewEntity savedReview = reviewRepository.findById(reviewId).orElseThrow();
		assertThat(savedReview.getRating()).isEqualTo("5");
		assertThat(savedReview.getContent()).isEqualTo("reviewContent");
		assertThat(savedReview.getImage()).isNull();
	}

	@DisplayName("리뷰 생성 시, appointmentId로 리뷰가 저장되고 reviewId를 반환한다. (이미지 있음)")
	@Test
	void givenValidRequestWithImage_whenCreateReview_thenSaveReviewWithS3Key() {
		//given
		// owner
		OwnerEntity owner = OwnerFixture.createOwnerEntity();
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = ShopFixture.createShopEntity(owner);
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = DesignerFixture.createDesignerEntity();
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		AppointmentEntity appointment = AppointmentFixture.createAppointmentEntity(client, designerShop, clock);
		appointmentRepository.save(appointment);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		String reviewRating = "5";
		String reviewContent = "reviewContent";

		ReviewCreateRequest request = new ReviewCreateRequest(appointment.getId(), reviewRating, reviewContent);

		// S3Client
		given(s3Client.putObject(
			any(Consumer.class),
			any(RequestBody.class)
		)).willReturn(PutObjectResponse.builder().build());

		// reviewImage
		MockMultipartFile fakeReviewImage = new MockMultipartFile(
			"fakeReviewImage",
			"test.png",
			"image/png",
			"fake image".getBytes()
		);

		//when
		UUID reviewId = reviewService.createReview(request, fakeReviewImage);

		//then
		ReviewEntity savedReview = reviewRepository.findById(reviewId).orElseThrow();
		assertThat(savedReview.getRating()).isEqualTo("5");
		assertThat(savedReview.getContent()).isEqualTo("reviewContent");

		// s3key
		assertThat(savedReview.getImage()).isNotNull();
		assertThat(savedReview.getImage()).endsWith(".png");
		verify(s3Client, times(1)).putObject(
			any(Consumer.class),
			any(RequestBody.class)
		);
	}

	@DisplayName("리뷰 생성 시, appointmentId가 존재하지 않으면 예외가 발생한다.")
	@Test
	void givenInvalidAppointmentId_whenCreateReview_thenThrowException() {
		//given
		// owner
		OwnerEntity owner = OwnerFixture.createOwnerEntity();
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = ShopFixture.createShopEntity(owner);
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = DesignerFixture.createDesignerEntity();
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		UUID invalidAppointmentId = UUID.randomUUID();
		String reviewRating = "5";
		String reviewContent = "reviewContent";

		ReviewCreateRequest request = new ReviewCreateRequest(invalidAppointmentId, reviewRating, reviewContent);

		// when
		// then
		assertThatThrownBy(() -> reviewService.createReview(request, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Appointment not found");
	}

	@DisplayName("Role이 OWNER일 경우, ownerId 기준으로 리뷰 목록을 반환한다.")
	@Test
	void givenOwnerRole_whenGetReviewList_thenReturnReviewsByOwner() {
		//given
		// owner
		OwnerEntity owner = OwnerFixture.createOwnerEntity();
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = ShopFixture.createShopEntity(owner);
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = DesignerFixture.createDesignerEntity();
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		AppointmentEntity appointment = AppointmentFixture.createAppointmentEntity(client, designerShop, clock);
		appointmentRepository.save(appointment);

		// review

		ReviewEntity review = ReviewFixture.createReviewEntity(appointment);
		reviewRepository.save(review);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(owner.getId(), Role.OWNER);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<ReviewListResponse> reviewList = reviewService.getReviewList();

		//then
		assertThat(reviewList).hasSize(1);
		assertThat(reviewList.get(0).reviewRating()).isEqualTo("5");
		assertThat(reviewList.get(0).reviewContent()).isEqualTo("reviewContent");
		assertThat(reviewList.get(0).reviewImage()).isEqualTo("/img.png");
		assertThat(reviewList.get(0).shopName()).isEqualTo("shopName");
		assertThat(reviewList.get(0).designerNickName()).isEqualTo("designerNickName");
		assertThat(reviewList.get(0).serviceName()).isEqualTo("serviceName");
	}

	@DisplayName("Role이 DESIGNER일 경우, designerId 기준으로 리뷰 목록을 반환한다.")
	@Test
	void givenDesignerRole_whenGetReviewList_thenReturnReviewsByDesigner() {
		// given
		// owner
		OwnerEntity owner = OwnerFixture.createOwnerEntity();
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = ShopFixture.createShopEntity(owner);
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = DesignerFixture.createDesignerEntity();
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		AppointmentEntity appointment = AppointmentFixture.createAppointmentEntity(client, designerShop, clock);
		appointmentRepository.save(appointment);

		// review
		ReviewEntity review = ReviewFixture.createReviewEntity(appointment);
		reviewRepository.save(review);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(designer.getId(), Role.DESIGNER);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<ReviewListResponse> reviewList = reviewService.getReviewList();

		//then
		assertThat(reviewList).hasSize(1);
		assertThat(reviewList.get(0).reviewRating()).isEqualTo("5");
		assertThat(reviewList.get(0).reviewContent()).isEqualTo("reviewContent");
		assertThat(reviewList.get(0).reviewImage()).isEqualTo("/img.png");
		assertThat(reviewList.get(0).shopName()).isEqualTo("shopName");
		assertThat(reviewList.get(0).designerNickName()).isEqualTo("designerNickName");
		assertThat(reviewList.get(0).serviceName()).isEqualTo("serviceName");
	}

}
