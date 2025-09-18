package com.seomse.interaction.appointment.controller.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NormalAppointmentCreateRequest(

	@NotNull(message = "shopId is required.")
	UUID shopId,

	@NotNull(message = "designerId is required.")
	UUID designerId,

	@NotNull(message = "appointmentDate is required.")
	LocalDate appointmentDate,

	@NotNull(message = "appointmentTime is required.")
	LocalTime appointmentTime,

	@NotBlank(message = "serviceName must not be blank.")
	String serviceName
) {
}
