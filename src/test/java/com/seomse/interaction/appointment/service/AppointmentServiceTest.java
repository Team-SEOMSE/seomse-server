package com.seomse.interaction.appointment.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

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
import com.seomse.s3.service.S3Service;
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

class AppointmentServiceTest extends IntegrationTestSupport {

	@Autowired
	private AppointmentService appointmentService;

	@MockitoBean
	private S3Service s3Service;

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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"),
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		when(securityService.getCurrentLoginUserInfo()).thenReturn(fakeLoginUser);

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

		// requirementsImage
		MockMultipartFile fakeRequirementsImage = new MockMultipartFile(
			"requirementsImage",
			"test.png",
			"image/png",
			"bytes".getBytes()
		);

		// when
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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"),
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		when(securityService.getCurrentLoginUserInfo()).thenReturn(fakeLoginUser);

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

		// requirementsImage
		MockMultipartFile fakeRequirementsImage = new MockMultipartFile(
			"requirementsImage",
			"test.png",
			"image/png",
			"fake image".getBytes()
		);

		String expectedS3Key = "/images.png";
		when(s3Service.upload(any(MultipartFile.class))).thenReturn(expectedS3Key);

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
		assertThat(appointmentDetail.getRequirementsImage()).isEqualTo(expectedS3Key);
		verify(s3Service, times(1)).upload(fakeRequirementsImage);
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
		when(securityService.getCurrentLoginUserInfo()).thenReturn(fakeLoginUser);

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
		ClientEntity client = new ClientEntity("user@email.com", bCryptPasswordEncoder.encode("abc1234!"),
			SnsType.NORMAL, null, null);
		clientRepository.save(client);

		LoginUserInfo fakeLoginUser = new LoginUserInfo(client.getId(), Role.CLIENT);

		when(securityService.getCurrentLoginUserInfo()).thenReturn(fakeLoginUser);

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

}