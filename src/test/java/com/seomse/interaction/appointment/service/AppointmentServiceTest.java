package com.seomse.interaction.appointment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import com.seomse.IntegrationTestSupport;
import com.seomse.config.TestClockConfig;
import com.seomse.fixture.interaction.appointment.AppointmentDetailFixture;
import com.seomse.fixture.interaction.appointment.AppointmentFixture;
import com.seomse.fixture.interaction.review.ReviewFixture;
import com.seomse.fixture.shop.ShopFixture;
import com.seomse.fixture.user.client.ClientFixture;
import com.seomse.fixture.user.designer.DesignerFixture;
import com.seomse.fixture.user.owner.OwnerFixture;
import com.seomse.interaction.appointment.controller.request.AppointmentDateRequest;
import com.seomse.interaction.appointment.controller.request.NormalAppointmentCreateRequest;
import com.seomse.interaction.appointment.controller.request.SpecialAppointmentCreateRequest;
import com.seomse.interaction.appointment.entity.AppointmentDetailEntity;
import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;
import com.seomse.interaction.appointment.repository.AppointmentDetailRepository;
import com.seomse.interaction.appointment.repository.AppointmentRepository;
import com.seomse.interaction.appointment.service.response.AppointmentDetailResponse;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;
import com.seomse.interaction.appointment.service.response.AppointmentTimeListResponse;
import com.seomse.interaction.review.entity.ReviewEntity;
import com.seomse.interaction.review.repository.ReviewRepository;
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

@Import(TestClockConfig.class)
class AppointmentServiceTest extends IntegrationTestSupport {

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private Clock clock;

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
	private AppointmentDetailRepository appointmentDetailRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@AfterEach
	void tearDown() {
		reviewRepository.deleteAll();
		appointmentDetailRepository.deleteAll();
		appointmentRepository.deleteAll();
		designerShopRepository.deleteAll();
		designerRepository.deleteAll();
		shopRepository.deleteAll();
		ownerRepository.deleteAll();
		clientRepository.deleteAll();
	}

	@DisplayName("special 헤어 예약 생성 시, appointmentId를 반환한다. (Client, DesignerShop 있음 + 이미지 없음)")
	@Test
	void givenValidRequestWithoutImage_whenCreateAppointment_thenReturnAppointmentId() {
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

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		LocalDate testDate = LocalDate.now(clock).plusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);

		SpecialAppointmentCreateRequest request = new SpecialAppointmentCreateRequest(
			shop.getId(),
			designerShop.getDesigner().getId(),
			testDate,
			testTime,
			"커트",
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"말 걸지 말아주세요."
		);

		// when
		UUID appointmentId = appointmentService.createSpecialAppointment(request, null);

		// then
		// appointment
		AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow();
		assertThat(appointment.getAppointmentDate()).isEqualTo(testDate);
		assertThat(appointment.getAppointmentTime()).isEqualTo(testTime);
		assertThat(appointment.getServiceName()).isEqualTo("커트");
		assertThat(appointment.getClient().getId()).isEqualTo(client.getId());
		assertThat(appointment.getDesignerShop().getId()).isEqualTo(designerShop.getId());

		// appointmentDetail
		AppointmentDetailEntity appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointmentId)
			.orElseThrow();
		assertThat(appointmentDetail.getRequirements()).isEqualTo("말 걸지 말아주세요.");
		assertThat(appointmentDetail.getScaleType()).isEqualTo(ScaleType.DRY);
		assertThat(appointmentDetail.getHairType()).isEqualTo(HairType.CURLY);
		assertThat(appointmentDetail.getHairLength()).isEqualTo(HairLength.MEDIUM);
		assertThat(appointmentDetail.getHairTreatmentType()).isEqualTo(HairTreatmentType.BLEACH);
		assertThat(appointmentDetail.getRequirementsImage()).isNull();
	}

	@DisplayName("special 헤어 예약 생성 시, appointmentId를 반환한다. (Client, DesignerShop 있음 + 이미지 있음)")
	@Test
	void givenValidRequestWithImage_whenCreateAppointment_thenReturnAppointmentId() throws IOException {
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

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		LocalDate testDate = LocalDate.now(clock).plusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);

		SpecialAppointmentCreateRequest request = new SpecialAppointmentCreateRequest(
			shop.getId(),
			designerShop.getDesigner().getId(),
			testDate,
			testTime,
			"커트",
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"말 걸지 말아주세요."
		);

		// S3Client
		given(s3Client.putObject(
			any(Consumer.class),
			any(RequestBody.class)
		)).willReturn(PutObjectResponse.builder().build());

		// requirementsImage
		MockMultipartFile fakeRequirementsImage = new MockMultipartFile(
			"requirementsImage",
			"test.png",
			"image/png",
			"fake image".getBytes()
		);

		// when
		// appointment
		UUID appointmentId = appointmentService.createSpecialAppointment(request, fakeRequirementsImage);

		// then
		// appointment
		AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow();
		assertThat(appointment.getAppointmentDate()).isEqualTo(testDate);
		assertThat(appointment.getAppointmentTime()).isEqualTo(testTime);
		assertThat(appointment.getServiceName()).isEqualTo("커트");
		assertThat(appointment.getClient().getId()).isEqualTo(client.getId());
		assertThat(appointment.getDesignerShop().getId()).isEqualTo(designerShop.getId());

		// appointmentDetail
		AppointmentDetailEntity appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointmentId)
			.orElseThrow();
		assertThat(appointmentDetail.getRequirements()).isEqualTo("말 걸지 말아주세요.");
		assertThat(appointmentDetail.getScaleType()).isEqualTo(ScaleType.DRY);
		assertThat(appointmentDetail.getHairType()).isEqualTo(HairType.CURLY);
		assertThat(appointmentDetail.getHairLength()).isEqualTo(HairLength.MEDIUM);
		assertThat(appointmentDetail.getHairTreatmentType()).isEqualTo(HairTreatmentType.BLEACH);

		// s3key
		assertThat(appointmentDetail.getRequirementsImage()).isNotNull();
		assertThat(appointmentDetail.getRequirementsImage()).endsWith(".png");
		verify(s3Client, times(1)).putObject(
			any(Consumer.class),
			any(RequestBody.class)
		);
	}

	@DisplayName("normal 헤어 예약 생성 시, 존재하지 않는 Client ID로 예약을 생성하면 예외가 발생한다.")
	@Test
	void givenInvalidClientId_whenCreateNormalAppointment_thenThrowUserNotFoundException() {
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
		UUID nonExistClientId = UUID.randomUUID();
		LoginUserInfo fakeLoginUser = new LoginUserInfo(nonExistClientId, Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//request
		LocalDate testDate = LocalDate.now(clock).plusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);

		NormalAppointmentCreateRequest request = new NormalAppointmentCreateRequest(
			shop.getId(),
			designerShop.getDesigner().getId(),
			testDate,
			testTime,
			"커트"
		);
		//when
		//then
		assertThatThrownBy(() -> appointmentService.createNormalAppointment(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("User not found.");
	}

	@DisplayName("normal 헤어 예약 생성 시, 존재하지 않는 디자이너 ID로 예약을 생성하면 예외가 발생한다.")
	@Test
	void givenInvalidDesignerId_whenCreateNormalAppointment_thenThrowDesignerShopNotFoundException() {
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

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//request
		LocalDate testDate = LocalDate.now(clock).plusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);

		NormalAppointmentCreateRequest request = new NormalAppointmentCreateRequest(
			shop.getId(),
			designer.getId(),
			testDate,
			testTime,
			"커트"
		);
		//when
		//then
		assertThatThrownBy(() -> appointmentService.createNormalAppointment(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("DesignerShop not found.");
	}

	@DisplayName("normal 헤어 예약 생성 시, 과거 날짜/시간이면 예외가 발생한다.")
	@Test
	void givenPastDateTime_whenCreateNormalAppointment_thenThrowIllegalStateException() {
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

		// request
		LocalDate testDate = LocalDate.now(clock).minusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);

		NormalAppointmentCreateRequest request = new NormalAppointmentCreateRequest(
			shop.getId(),
			designer.getId(),
			testDate,
			testTime,
			"커트"
		);

		// when & then
		assertThatThrownBy(() -> appointmentService.createNormalAppointment(request))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Appointment date must be after today.");
	}

	@DisplayName("normal 헤어 예약 생성 시, 동일한 날짜/시간에 이미 예약이 있으면 예외가 발생한다.")
	@Test
	void givenDuplicateDateTime_whenCreateNormalAppointment_thenThrowIllegalStateException() {
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

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// appointment
		AppointmentEntity existedAppointment = AppointmentFixture.createAppointmentEntity(client, designerShop, clock);
		appointmentRepository.save(existedAppointment);

		// request
		LocalDate testDate = LocalDate.now(clock).plusDays(1);
		LocalTime testTime = LocalTime.of(12, 0);

		NormalAppointmentCreateRequest request = new NormalAppointmentCreateRequest(
			shop.getId(),
			designer.getId(),
			testDate,
			testTime,
			"커트"
		);

		// when & then
		assertThatThrownBy(() -> appointmentService.createNormalAppointment(request))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Appointment already exists.");
	}

	@DisplayName("예약 전체 조회 시 리뷰가 있는 Client 예약을 반환한다.")
	@Test
	void givenValidRequest_whenGetAppointmentList_thenReturnClientAppointmentList() {
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

		// DesignerShop
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

		// review
		ReviewEntity review = ReviewFixture.createReviewEntity(appointment);
		reviewRepository.save(review);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		assertThat(appointmentList).hasSize(1);
		assertThat(appointmentList.get(0).appointmentId()).isEqualTo(appointment.getId());
		assertThat(appointmentList.get(0).shopName()).isEqualTo("shopName");
		assertThat(appointmentList.get(0).designerNickname()).isEqualTo("designerNickName");
		assertThat(appointmentList.get(0).serviceName()).isEqualTo("serviceName");
		assertThat(appointmentList.get(0).appointmentDate()).isEqualTo(LocalDate.now(clock).plusDays(1));
		assertThat(appointmentList.get(0).appointmentTime()).isEqualTo(LocalTime.of(12, 0));
		assertThat(appointmentList.get(0).hasReview()).isTrue();
	}

	@DisplayName("예약 전체 조회 시 리뷰가 있는 Owner 예약을 반환한다.")
	@Test
	void givenValidRequest_whenGetAppointmentList_thenReturnOwnerAppointmentList() {
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

		// DesignerShop
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
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).hasSize(1);
		assertThat(appointmentList.get(0).appointmentId()).isEqualTo(appointment.getId());
		assertThat(appointmentList.get(0).shopName()).isEqualTo("shopName");
		assertThat(appointmentList.get(0).designerNickname()).isEqualTo("designerNickName");
		assertThat(appointmentList.get(0).serviceName()).isEqualTo("serviceName");
		assertThat(appointmentList.get(0).appointmentDate()).isEqualTo(LocalDate.now(clock).plusDays(1));
		assertThat(appointmentList.get(0).appointmentTime()).isEqualTo(LocalTime.of(12, 0));
		assertThat(appointmentList.get(0).hasReview()).isTrue();
	}

	@DisplayName("예약 전체 조회 시 리뷰가 있는 Designer 예약을 반환한다.")
	@Test
	void givenValidRequest_whenGetAppointmentList_thenReturnDesignerAppointmentList() {
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

		// DesignerShop
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
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).hasSize(1);
		assertThat(appointmentList.get(0).appointmentId()).isEqualTo(appointment.getId());
		assertThat(appointmentList.get(0).shopName()).isEqualTo("shopName");
		assertThat(appointmentList.get(0).designerNickname()).isEqualTo("designerNickName");
		assertThat(appointmentList.get(0).serviceName()).isEqualTo("serviceName");
		assertThat(appointmentList.get(0).appointmentDate()).isEqualTo(LocalDate.now(clock).plusDays(1));
		assertThat(appointmentList.get(0).appointmentTime()).isEqualTo(LocalTime.of(12, 0));
		assertThat(appointmentList.get(0).hasReview()).isTrue();
	}

	@DisplayName("예약 전체 조회 시 Client 예약이 없으면 빈 리스트를 반환한다.")
	@Test
	void givenNoClientAppointments_whenGetAppointmentList_thenReturnEmptyList() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(designer.getId(), Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).isEmpty();
	}

	@DisplayName("예약 전체 조회 시 Owner 예약이 없으면 빈 리스트를 반환한다.")
	@Test
	void givenNoOwnerAppointments_whenGetAppointmentList_thenReturnEmptyList() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(designer.getId(), Role.OWNER);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).isEmpty();
	}

	@DisplayName("예약 전체 조회 시 Designer 예약이 없으면 빈 리스트를 반환한다.")
	@Test
	void givenNoDesignerAppointments_whenGetAppointmentList_thenReturnEmptyList() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(designer.getId(), Role.DESIGNER);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).isEmpty();
	}

	@DisplayName("예약 상세 조회에 성공하면 AppointmentDetailResponse를 반환한다.")
	@Test
	void givenValidAppointmentId_whenGetAppointment_thenReturnDetailResponse() {
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

		// appointmentDetail
		AppointmentDetailEntity appointmentDetail = AppointmentDetailFixture.createAppointmentDetailEntity(appointment);
		appointmentDetailRepository.save(appointmentDetail);

		UUID appointmentId = appointmentDetail.getAppointment().getId();

		// when
		AppointmentDetailResponse response = appointmentService.getAppointment(appointmentId);

		// then
		assertThat(response.scaleType()).isEqualTo(appointmentDetail.getScaleType());
		assertThat(response.hairType()).isEqualTo(appointmentDetail.getHairType());
		assertThat(response.hairLength()).isEqualTo(appointmentDetail.getHairLength());
		assertThat(response.hairTreatmentType()).isEqualTo(appointmentDetail.getHairTreatmentType());
		assertThat(response.requirements()).isEqualTo(appointmentDetail.getRequirements());
		assertThat(response.requirementsImage()).isEqualTo(appointmentDetail.getRequirementsImage());
	}

	@DisplayName("예약 상세 조회에 실패하면 IllegalArgumentException이 발생한다.")
	@Test
	void givenInvalidAppointmentId_whenGetAppointment_thenThrowException() {
		// given
		UUID invalidAppointmentId = UUID.randomUUID();

		// when & then
		assertThatThrownBy(() -> appointmentService.getAppointment(invalidAppointmentId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Appointment not found.");
	}

	@DisplayName("예약 최신 상세 조회에 성공하면 최신의 AppointmentDetailResponse를 하나 반환한다.")
	@Test
	void givenValidAppointmentId_whenGetLatestAppointment_thenReturnLatestDetailResponse() {
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

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// appointment
		AppointmentEntity appointment = AppointmentFixture.createAppointmentEntity(client, designerShop, clock);
		appointmentRepository.save(appointment);

		// appointmentDetail
		AppointmentDetailEntity appointmentDetail = AppointmentDetailFixture.createAppointmentDetailEntity(appointment);
		appointmentDetailRepository.save(appointmentDetail);

		// when
		AppointmentDetailResponse response = appointmentService.getAppointmentByLatest();

		// then
		assertThat(response.scaleType()).isEqualTo(appointmentDetail.getScaleType());
		assertThat(response.hairType()).isEqualTo(appointmentDetail.getHairType());
		assertThat(response.hairLength()).isEqualTo(appointmentDetail.getHairLength());
		assertThat(response.hairTreatmentType()).isEqualTo(appointmentDetail.getHairTreatmentType());
		assertThat(response.requirements()).isEqualTo(appointmentDetail.getRequirements());
		assertThat(response.requirementsImage()).isEqualTo(appointmentDetail.getRequirementsImage());
	}

	@DisplayName("예약 최신 상세 조회에 실패하면 IllegalArgumentException이 발생한다.")
	@Test
	void givenInvalidAppointmentId_whenGetLatestAppointment_thenThrowException() {
		// given
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// when & then
		assertThatThrownBy(() -> appointmentService.getAppointmentByLatest())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Appointment not found.");
	}

	@DisplayName("예약 시간을 조회할 때, 오늘 선택 시 현재 이후 시간만 반환한다")
	@Test
	void givenTodayDate_whenGetAppointmentTimes_thenReturnTimesAfterNow() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		// 테스트 시간: 2025, 9, 17, 12, 0
		LocalDate today = LocalDate.now(clock);

		// 과거 예약
		AppointmentEntity pastAppointment = new AppointmentEntity(client, designerShop,
			today.minusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(pastAppointment);

		// 오늘 예약
		AppointmentEntity todayAppointment = new AppointmentEntity(client, designerShop,
			today, LocalTime.of(13, 0), "serviceName");
		appointmentRepository.save(todayAppointment);

		// 미래 예약
		AppointmentEntity futureAppointment = new AppointmentEntity(client, designerShop,
			today.plusDays(1), LocalTime.of(14, 0), "serviceName");
		appointmentRepository.save(futureAppointment);

		// request
		AppointmentDateRequest request = new AppointmentDateRequest(shop.getId(), designer.getId(), today);

		// when
		List<AppointmentTimeListResponse> appointmentTimeList = appointmentService.getAppointmentByDesignerAndDateTime(
			request);

		// then
		assertThat(appointmentTimeList).hasSize(1);
		assertThat(appointmentTimeList.get(0).appointmentTime()).isEqualTo(LocalTime.of(13, 0));
	}

	@DisplayName("예약 시간을 조회할 때, 미래 선택 시 그날의 예약을 반환한다")
	@Test
	void givenFutureDate_whenGetAppointmentTimes_thenReturnTimes() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		// 테스트 시간: 2025, 9, 17, 12, 0
		LocalDate today = LocalDate.now(clock);

		// 과거 예약
		AppointmentEntity pastAppointment = new AppointmentEntity(client, designerShop,
			today.minusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(pastAppointment);

		// 오늘 예약
		AppointmentEntity todayAppointment = new AppointmentEntity(client, designerShop,
			today, LocalTime.of(13, 0), "serviceName");
		appointmentRepository.save(todayAppointment);

		// 미래 예약
		AppointmentEntity futureAppointment = new AppointmentEntity(client, designerShop,
			today.plusDays(1), LocalTime.of(14, 0), "serviceName");
		appointmentRepository.save(futureAppointment);

		// request
		AppointmentDateRequest request = new AppointmentDateRequest(shop.getId(), designer.getId(), today.plusDays(1));

		// when
		List<AppointmentTimeListResponse> appointmentTimeList = appointmentService.getAppointmentByDesignerAndDateTime(
			request);

		// then
		assertThat(appointmentTimeList).hasSize(1);
		assertThat(appointmentTimeList.get(0).appointmentTime()).isEqualTo(LocalTime.of(14, 0));
	}

	@DisplayName("예약 시간을 조회할 때, 과거 선택 시 IllegalArgumentException이 발생한다")
	@Test
	void givenPastDate_whenGetAppointmentTimes_thenThrowException() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		// 테스트 시간: 2025, 9, 17, 12, 0
		LocalDate today = LocalDate.now(clock);

		// 과거 예약
		AppointmentEntity pastAppointment = new AppointmentEntity(client, designerShop,
			today.minusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(pastAppointment);

		// 오늘 예약
		AppointmentEntity todayAppointment = new AppointmentEntity(client, designerShop,
			today, LocalTime.of(13, 0), "serviceName");
		appointmentRepository.save(todayAppointment);

		// 미래 예약
		AppointmentEntity futureAppointment = new AppointmentEntity(client, designerShop,
			today.plusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(futureAppointment);

		// request
		AppointmentDateRequest request = new AppointmentDateRequest(shop.getId(), designer.getId(), today.minusDays(1));

		// when
		// then
		assertThatThrownBy(() -> appointmentService.getAppointmentByDesignerAndDateTime(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("past date is not allowed.");
	}

	@DisplayName("예약 시간을 조회할 때, 오늘일 경우 현재 시각과 같은 예약은 반환하지 않는다")
	@Test
	void givenTodayDate_whenGetAppointmentTimes_thenExcludeCurrentTimes() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		// 테스트 시간: 2025, 9, 17, 12, 0
		LocalDate today = LocalDate.now(clock);

		// 과거 예약
		AppointmentEntity pastAppointment = new AppointmentEntity(client, designerShop,
			today.minusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(pastAppointment);

		// 오늘 예약
		AppointmentEntity todayAppointment = new AppointmentEntity(client, designerShop,
			today, LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(todayAppointment);

		// 미래 예약
		AppointmentEntity futureAppointment = new AppointmentEntity(client, designerShop,
			today.plusDays(1), LocalTime.of(14, 0), "serviceName");
		appointmentRepository.save(futureAppointment);

		// request
		AppointmentDateRequest request = new AppointmentDateRequest(shop.getId(), designer.getId(), today);

		// when
		List<AppointmentTimeListResponse> appointmentTimeList = appointmentService.getAppointmentByDesignerAndDateTime(
			request);

		// then
		assertThat(appointmentTimeList).hasSize(0);
	}

	@DisplayName("존재하지 않는 ShopId로 예약 시간을 조회할 때, IllegalArgumentException이 발생한다")
	@Test
	void givenInvalidShopId_whenGetAppointmentTimes_thenThrowException() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		// 테스트 시간: 2025, 9, 17, 12, 0
		LocalDate today = LocalDate.now(clock);

		// 과거 예약
		AppointmentEntity pastAppointment = new AppointmentEntity(client, designerShop,
			today.minusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(pastAppointment);

		// 오늘 예약
		AppointmentEntity todayAppointment = new AppointmentEntity(client, designerShop,
			today, LocalTime.of(13, 0), "serviceName");
		appointmentRepository.save(todayAppointment);

		// 미래 예약
		AppointmentEntity futureAppointment = new AppointmentEntity(client, designerShop,
			today.plusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(futureAppointment);

		// request
		UUID invalidShopId = UUID.randomUUID();
		AppointmentDateRequest request = new AppointmentDateRequest(invalidShopId, designer.getId(),
			today.minusDays(1));

		// when
		// then
		assertThatThrownBy(() -> appointmentService.getAppointmentByDesignerAndDateTime(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Shop not found.");
	}

	@DisplayName("존재하지 않는 designerId로 예약 시간을 조회할 때, IllegalArgumentException이 발생한다")
	@Test
	void givenInvalidDesignerId_whenGetAppointmentTimes_thenThrowException() {
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

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = ClientFixture.createClient();
		clientRepository.save(client);

		// appointment
		// 테스트 시간: 2025, 9, 17, 12, 0
		LocalDate today = LocalDate.now(clock);

		// 과거 예약
		AppointmentEntity pastAppointment = new AppointmentEntity(client, designerShop,
			today.minusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(pastAppointment);

		// 오늘 예약
		AppointmentEntity todayAppointment = new AppointmentEntity(client, designerShop,
			today, LocalTime.of(13, 0), "serviceName");
		appointmentRepository.save(todayAppointment);

		// 미래 예약
		AppointmentEntity futureAppointment = new AppointmentEntity(client, designerShop,
			today.plusDays(1), LocalTime.of(12, 0), "serviceName");
		appointmentRepository.save(futureAppointment);

		// request
		UUID invalidDesignerId = UUID.randomUUID();
		AppointmentDateRequest request = new AppointmentDateRequest(shop.getId(), invalidDesignerId,
			today.minusDays(1));

		// when
		// then
		assertThatThrownBy(() -> appointmentService.getAppointmentByDesignerAndDateTime(request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Designer not found.");
	}

}
