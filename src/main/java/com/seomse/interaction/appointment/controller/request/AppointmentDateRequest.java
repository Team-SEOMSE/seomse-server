package com.seomse.interaction.appointment.controller.request;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

public record AppointmentDateRequest(

	@NotNull(message = "shopId is required")
	UUID shopId,

	@NotNull(message = "designerId is required")
	UUID designerId,

	@NotNull(message = "appointmentDate is required")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	LocalDate appointmentDate
) {
}
