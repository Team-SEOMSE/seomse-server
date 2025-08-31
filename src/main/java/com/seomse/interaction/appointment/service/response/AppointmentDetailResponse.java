package com.seomse.interaction.appointment.service.response;

import com.seomse.interaction.appointment.enums.HairLength;
import com.seomse.interaction.appointment.enums.HairTreatmentType;
import com.seomse.interaction.appointment.enums.HairType;
import com.seomse.interaction.appointment.enums.ScaleType;

public record AppointmentDetailResponse(
	ScaleType scaleType,
	HairType hairType,
	HairLength hairLength,
	HairTreatmentType hairTreatmentType,
	String requirements,
	String requirementsImage
) {
}
