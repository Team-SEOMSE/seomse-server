package com.seomse.interaction.appointment.controller.request;

import java.util.UUID;

import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppointmentCreateRequest(
	@NotNull(message = "shopId is required.")
	UUID shopId,

	@NotNull(message = "designerId is required.")
	UUID designerId,

	@NotBlank(message = "serviceName must not be blank.")
	String serviceName,

	@NotNull(message = "scaleType is required.")
	ScaleType scaleType,

	@NotNull(message = "hairType is required.")
	HairType hairType,

	@NotNull(message = "hairLength is required.")
	HairLength hairLength,

	@NotNull(message = "hairTreatmentType is required.")
	HairTreatmentType hairTreatmentType,

	String requirements
) {
}
