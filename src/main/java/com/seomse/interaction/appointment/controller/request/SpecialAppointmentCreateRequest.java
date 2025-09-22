package com.seomse.interaction.appointment.controller.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;

import jakarta.validation.constraints.NotNull;

public record SpecialAppointmentCreateRequest(

	UUID shopId,

	UUID designerId,

	LocalDate appointmentDate,

	LocalTime appointmentTime,

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
) implements AppointmentBaseRequest {
}
