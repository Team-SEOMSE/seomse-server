package com.seomse.interaction.appointment.controller.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AppointmentDateRequest(

	@NotNull(message = "shopId is required")
	UUID shopId,

	@NotNull(message = "designerId is required")
	UUID designerId,

	@NotNull(message = "appointmentDate is required")
	LocalDate appointmentDate
) {
}
