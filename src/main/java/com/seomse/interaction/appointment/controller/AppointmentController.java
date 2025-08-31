package com.seomse.interaction.appointment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.common.controller.ApiResponse;
import com.seomse.interaction.appointment.controller.request.AppointmentCreateRequest;
import com.seomse.interaction.appointment.service.AppointmentService;
import com.seomse.interaction.appointment.service.response.AppointmentDetailResponse;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/interaction/appointments")
@RestController
public class AppointmentController {

	private final AppointmentService appointmentService;

	@PostMapping
	public ApiResponse<UUID> createAppointment(
		@Valid @RequestPart AppointmentCreateRequest request,
		@RequestPart(required = false) MultipartFile requirementsImage) {
		return ApiResponse.created(appointmentService.createAppointment(request, requirementsImage));
	}

	@GetMapping
	public ApiResponse<List<AppointmentListResponse>> getAppointmentList() {
		return ApiResponse.ok(appointmentService.getAppointmentList());
	}

	@GetMapping("/{appointmentId}/details")
	public ApiResponse<AppointmentDetailResponse> getAppointment(@PathVariable UUID appointmentId) {
		return ApiResponse.ok(appointmentService.getAppointment(appointmentId));
	}

	@GetMapping("/details")
	public ApiResponse<AppointmentDetailResponse> getAppointmentByLatest() {
		return ApiResponse.ok(appointmentService.getAppointmentByLatest());
	}
}
