package com.seomse.interaction.appointment.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.interaction.appointment.controller.request.AppointmentBaseRequest;
import com.seomse.interaction.appointment.controller.request.NormalAppointmentCreateRequest;
import com.seomse.interaction.appointment.controller.request.SpecialAppointmentCreateRequest;
import com.seomse.interaction.appointment.entity.AppointmentDetailEntity;
import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.appointment.repository.AppointmentDetailRepository;
import com.seomse.interaction.appointment.repository.AppointmentQueryRepository;
import com.seomse.interaction.appointment.repository.AppointmentRepository;
import com.seomse.interaction.appointment.service.response.AppointmentDetailResponse;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;
import com.seomse.s3.service.S3Service;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.security.service.SecurityService;
import com.seomse.shop.entity.DesignerShopEntity;
import com.seomse.shop.repository.DesignerShopRepository;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class AppointmentService {

	private final SecurityService securityService;
	private final S3Service s3Service;
	private final ClientRepository clientRepository;
	private final DesignerShopRepository designerShopRepository;
	private final AppointmentRepository appointmentRepository;
	private final AppointmentDetailRepository appointmentDetailRepository;
	private final AppointmentQueryRepository appointmentQueryRepository;

	public UUID createNormalAppointment(NormalAppointmentCreateRequest request) {
		return createBaseAppointment(request).getId();
	}

	public UUID createSpecialAppointment(SpecialAppointmentCreateRequest request,
		MultipartFile requirementsImage) {
		AppointmentEntity appointment = createBaseAppointment(request);

		String s3Key = null;
		if (requirementsImage != null && !requirementsImage.isEmpty()) {
			final String S3_FOLDER = "appointment";
			try {
				s3Key = s3Service.upload(requirementsImage, S3_FOLDER);
			} catch (IOException e) {
				throw new RuntimeException("Failed to upload requirements image.", e);
			}
		}

		AppointmentDetailEntity appointmentDetail = new AppointmentDetailEntity(appointment, request.scaleType(),
			request.hairType(), request.hairLength(), request.hairTreatmentType(), request.requirements(), s3Key);

		appointmentDetailRepository.save(appointmentDetail);

		return appointment.getId();
	}

	private AppointmentEntity createBaseAppointment(AppointmentBaseRequest request) {
		LoginUserInfo loginUser = securityService.getCurrentLoginUserInfo();

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime appointmentDateTime = LocalDateTime.of(request.appointmentDate(), request.appointmentTime());

		if (appointmentDateTime.isBefore(now)) {
			throw new IllegalStateException("Appointment date must be after today.");
		}

		DesignerShopEntity designerShop = designerShopRepository
			.findByDesignerIdAndShopId(request.designerId(), request.shopId())
			.orElseThrow(() -> new IllegalArgumentException("DesignerShop not found."));

		Boolean exists = appointmentRepository.existsByDesignerShopIdAndAppointmentDateAndAppointmentTime(
			designerShop.getId(), request.appointmentDate(), request.appointmentTime());

		if (exists) {
			throw new IllegalStateException("Appointment already exists.");
		}

		ClientEntity client = clientRepository.findById(loginUser.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));

		AppointmentEntity appointment = new AppointmentEntity(client, designerShop,
			request.appointmentDate(), request.appointmentTime(), request.serviceName());

		appointmentRepository.save(appointment);

		return appointment;
	}

	@Transactional(readOnly = true)
	public List<AppointmentListResponse> getAppointmentList() {
		LoginUserInfo loginUser = securityService.getCurrentLoginUserInfo();
		Role role = loginUser.role();

		// 고객, 디자이너, 점주
		switch (role) {
			case CLIENT -> {
				return appointmentQueryRepository.findAppointmentListByClientId(loginUser.userId());
			}
			case OWNER -> {
				return appointmentQueryRepository.findAppointmentListByOwnerId(loginUser.userId());
			}
			case DESIGNER -> {
				return appointmentQueryRepository.findAppointmentListByDesignerId(loginUser.userId());
			}
			default -> throw new IllegalStateException("Unsupported role.");
		}
	}

	@Transactional(readOnly = true)
	public AppointmentDetailResponse getAppointment(UUID appointmentId) {
		return appointmentQueryRepository.findAppointmentDetail(appointmentId)
			.orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
	}

	@Transactional(readOnly = true)
	public AppointmentDetailResponse getAppointmentByLatest() {
		LoginUserInfo loginUser = securityService.getCurrentLoginUserInfo();
		return appointmentQueryRepository.findAppointmentDetailByLatest(loginUser.userId())
			.orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
	}
}
