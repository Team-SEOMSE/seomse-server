package com.seomse.interaction.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.seomse.IntegrationTestSupport;
import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.appointment.repository.AppointmentRepository;
import com.seomse.interaction.review.controller.request.ReviewCreateRequest;
import com.seomse.interaction.review.entity.ReviewEntity;
import com.seomse.interaction.review.repository.ReviewRepository;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;
import com.seomse.shop.repository.DesignerShopRepository;
import com.seomse.shop.repository.ShopRepository;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.repository.ClientRepository;
import com.seomse.user.designer.entity.DesignerEntity;
import com.seomse.user.designer.repository.DesignerRepository;
import com.seomse.user.owner.entity.OwnerEntity;
import com.seomse.user.owner.repository.OwnerRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class ReviewServiceTest extends IntegrationTestSupport {

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

	void createReview() {
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
		OwnerEntity owner = new OwnerEntity("user1@email.com", "abc1234!");
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"),
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		// appointment
		String serviceName = "serviceName";
		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
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
		OwnerEntity owner = new OwnerEntity("user1@email.com", "abc1234!");
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"),
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		// appointment
		String serviceName = "serviceName";
		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
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
		OwnerEntity owner = new OwnerEntity("user1@email.com", "abc1234!");
		ownerRepository.save(owner);

		// shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// designerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"),
			SnsType.NORMAL, null, null);
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

}