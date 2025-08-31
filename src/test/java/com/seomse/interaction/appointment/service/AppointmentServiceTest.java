package com.seomse.interaction.appointment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
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
import com.seomse.interaction.appointment.controller.request.AppointmentCreateRequest;
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

class AppointmentServiceTest extends IntegrationTestSupport {

	@Autowired
	private AppointmentService appointmentService;

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
	private AppointmentDetailRepository appointmentDetailRepository;

	@AfterEach
	void tearDown() {
		appointmentDetailRepository.deleteAll();
		appointmentRepository.deleteAll();
		designerShopRepository.deleteAll();
		designerRepository.deleteAll();
		shopRepository.deleteAll();
		ownerRepository.deleteAll();
		clientRepository.deleteAll();
	}

	@DisplayName("헤어 예약 생성 시, appointmentId를 반환한다. (Client, DesignerShop 있음 + 이미지 없음)")
	@Test
	void givenValidRequestWithoutImage_whenCreateAppointment_thenReturnAppointmentId() {
		// given
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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		AppointmentCreateRequest request = new AppointmentCreateRequest(
			shop.getId(),
			designerShop.getDesigner().getId(),
			"커트",
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"말 걸지 말아주세요."
		);

		// when
		UUID appointmentId = appointmentService.createAppointment(request, null);

		// then
		// appointment
		AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow();
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

	@DisplayName("헤어 예약 생성 시, appointmentId를 반환한다. (Client, DesignerShop 있음 + 이미지 있음)")
	@Test
	void givenValidRequestWithImage_whenCreateAppointment_thenReturnAppointmentId() throws IOException {
		// given
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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// request
		AppointmentCreateRequest request = new AppointmentCreateRequest(
			shop.getId(),
			designerShop.getDesigner().getId(),
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
		UUID appointmentId = appointmentService.createAppointment(request, fakeRequirementsImage);

		// then
		// appointment
		AppointmentEntity appointment = appointmentRepository.findById(appointmentId).orElseThrow();
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

	@DisplayName("존재하지 않는 Client ID로 예약을 생성하면 예외가 발생한다.")
	@Test
	void givenInvalidClientId_whenCreateAppointment_thenThrowUserNotFoundException() {
		//given
		// Owner
		OwnerEntity owner = new OwnerEntity("owner@email.com", "abc1234!");
		ownerRepository.save(owner);

		// Shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// Designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		UUID nonExistClientId = UUID.randomUUID();
		LoginUserInfo fakeLoginUser = new LoginUserInfo(nonExistClientId, Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//request
		AppointmentCreateRequest request = new AppointmentCreateRequest(
			shop.getId(),
			designerShop.getDesigner().getId(),
			"커트",
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"말 걸지 말아주세요"
		);
		//when
		//then
		assertThatThrownBy(() -> appointmentService.createAppointment(request, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("User not found.");
	}

	@DisplayName("존재하지 않는 디자이너 ID로 예약을 생성하면 예외가 발생한다.")
	@Test
	void givenInvalidDesignerId_whenCreateAppointment_thenThrowDesignerShopNotFoundException() {
		//given
		// Owner
		OwnerEntity owner = new OwnerEntity("owner@email.com", "abc1234!");
		ownerRepository.save(owner);

		// Shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//request
		AppointmentCreateRequest request = new AppointmentCreateRequest(
			shop.getId(),
			designer.getId(),
			"커트",
			ScaleType.DRY,
			HairType.CURLY,
			HairLength.MEDIUM,
			HairTreatmentType.BLEACH,
			"말 걸지 말아주세요"
		);
		//when
		//then
		assertThatThrownBy(() -> appointmentService.createAppointment(request, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("DesignerShop not found.");
	}

	@DisplayName("예약 전체 조회 시 Client 예약을 반환한다.")
	@Test
	void givenValidRequest_whenGetAppointmentList_thenReturnClientAppointmentList() {
		//given
		// Owner
		OwnerEntity owner = new OwnerEntity("owner@email.com", "abc1234!");
		ownerRepository.save(owner);

		// Shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		// appointment
		String serviceName = "serviceName";
		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
		appointmentRepository.save(appointment);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		assertThat(appointmentList).hasSize(1);
		assertThat(appointmentList.get(0).appointmentId()).isEqualTo(appointment.getId());
		assertThat(appointmentList.get(0).shopName()).isEqualTo("shopName1");
		assertThat(appointmentList.get(0).designerNickname()).isEqualTo("designerNickName10");
		assertThat(appointmentList.get(0).serviceName()).isEqualTo("serviceName");
		assertThat(appointmentList.get(0).appointmentDate()).isNotNull();
	}

	@DisplayName("예약 전체 조회 시 Owner 예약을 반환한다.")
	@Test
	void givenValidRequest_whenGetAppointmentList_thenReturnOwnerAppointmentList() {
		//given
		// Owner
		OwnerEntity owner = new OwnerEntity("owner@email.com", "abc1234!");
		ownerRepository.save(owner);

		// Shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		// appointment
		String serviceName = "serviceName";
		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
		appointmentRepository.save(appointment);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(owner.getId(), Role.OWNER);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).hasSize(1);
		assertThat(appointmentList.get(0).appointmentId()).isEqualTo(appointment.getId());
		assertThat(appointmentList.get(0).shopName()).isEqualTo("shopName1");
		assertThat(appointmentList.get(0).designerNickname()).isEqualTo("designerNickName10");
		assertThat(appointmentList.get(0).serviceName()).isEqualTo("serviceName");
		assertThat(appointmentList.get(0).appointmentDate()).isNotNull();
	}

	@DisplayName("예약 전체 조회 시 Designer 예약을 반환한다.")
	@Test
	void givenValidRequest_whenGetAppointmentList_thenReturnDesignerAppointmentList() {
		//given
		// Owner
		OwnerEntity owner = new OwnerEntity("owner@email.com", "abc1234!");
		ownerRepository.save(owner);

		// Shop
		ShopEntity shop = new ShopEntity(owner, Type.HAIR_SALON, "shopName1", "info1", "/img1.png");
		shopRepository.save(shop);

		// designer
		DesignerEntity designer = new DesignerEntity("designer10@email.com", "designer101234!", "designerNickName10");
		designerRepository.save(designer);

		// DesignerShop
		DesignerShopEntity designerShop = new DesignerShopEntity(designer, shop);
		designerShopRepository.save(designerShop);

		// client
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		// appointment
		String serviceName = "serviceName";
		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
		appointmentRepository.save(appointment);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(designer.getId(), Role.DESIGNER);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		//when
		List<AppointmentListResponse> appointmentList = appointmentService.getAppointmentList();

		//then
		// response
		assertThat(appointmentList).hasSize(1);
		assertThat(appointmentList.get(0).appointmentId()).isEqualTo(appointment.getId());
		assertThat(appointmentList.get(0).shopName()).isEqualTo("shopName1");
		assertThat(appointmentList.get(0).designerNickname()).isEqualTo("designerNickName10");
		assertThat(appointmentList.get(0).serviceName()).isEqualTo("serviceName");
		assertThat(appointmentList.get(0).appointmentDate()).isNotNull();
	}

	@DisplayName("예약 상세 조회에 성공하면 AppointmentDetailResponse를 반환한다.")
	@Test
	void givenValidAppointmentId_whenGetAppointment_thenReturnDetailResponse() {
		// given
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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		// appointment
		String serviceName = "serviceName";

		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
		appointmentRepository.save(appointment);

		// appointmentDetail
		ScaleType scaleType = ScaleType.DRY;
		HairType hairType = HairType.CURLY;
		HairLength hairLength = HairLength.MEDIUM;
		HairTreatmentType hairTreatmentType = HairTreatmentType.BLEACH;
		String requirements = "requirements";
		String requirementsImage = "/image.png";

		AppointmentDetailEntity appointmentDetail = new AppointmentDetailEntity(appointment, scaleType, hairType,
			hairLength, hairTreatmentType, requirements, requirementsImage);
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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// appointment
		String serviceName = "serviceName";

		AppointmentEntity appointment = new AppointmentEntity(client, designerShop, serviceName);
		appointmentRepository.save(appointment);

		// appointmentDetail
		ScaleType scaleType = ScaleType.DRY;
		HairType hairType = HairType.CURLY;
		HairLength hairLength = HairLength.MEDIUM;
		HairTreatmentType hairTreatmentType = HairTreatmentType.BLEACH;
		String requirements = "requirements";
		String requirementsImage = "/image.png";

		AppointmentDetailEntity appointmentDetail = new AppointmentDetailEntity(appointment, scaleType, hairType,
			hairLength, hairTreatmentType, requirements, requirementsImage);
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
		ClientEntity client = new ClientEntity("nouser@email.com", bCryptPasswordEncoder.encode("abc1234!"), "김섬세",
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);
		given(securityService.getCurrentLoginUserInfo()).willReturn(fakeLoginUser);

		// when & then
		assertThatThrownBy(() -> appointmentService.getAppointmentByLatest())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Appointment not found.");
	}
}
