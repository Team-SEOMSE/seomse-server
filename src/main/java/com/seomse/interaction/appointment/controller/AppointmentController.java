package com.seomse.interaction.appointment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.common.controller.ApiResponse;
import com.seomse.interaction.appointment.controller.request.AppointmentDateRequest;
import com.seomse.interaction.appointment.controller.request.NormalAppointmentCreateRequest;
import com.seomse.interaction.appointment.controller.request.SpecialAppointmentCreateRequest;
import com.seomse.interaction.appointment.service.AppointmentService;
import com.seomse.interaction.appointment.service.response.AppointmentDetailResponse;
import com.seomse.interaction.appointment.service.response.AppointmentListResponse;
import com.seomse.interaction.appointment.service.response.AppointmentTimeListResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/interaction/appointments")
@RestController
public class AppointmentController {

	private final AppointmentService appointmentService;

	@PostMapping("/normal")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<UUID> createNormalAppointment(@Valid @RequestBody NormalAppointmentCreateRequest request) {
		return ApiResponse.created(appointmentService.createNormalAppointment(request));
	}

	@PostMapping("/special")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<UUID> createSpecialAppointment(
		@Valid @RequestPart SpecialAppointmentCreateRequest request,
		@RequestPart(required = false) MultipartFile requirementsImage) {
		return ApiResponse.created(appointmentService.createSpecialAppointment(request, requirementsImage));
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

	@GetMapping("/times")
	public ApiResponse<List<AppointmentTimeListResponse>> getAppointmentByDesignerAndDateTime(
		@Valid @ModelAttribute AppointmentDateRequest request) {
		return ApiResponse.ok(appointmentService.getAppointmentByDesignerAndDateTime(request));
	}
}
