package com.seomse.interaction.appointment.service.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentListResponse(
	UUID appointmentId,
	String shopName,
	String designerNickname,
	String serviceName,
	LocalDate appointmentDate,
	LocalTime appointmentTime,
	boolean hasReview
) {
}
