package com.seomse.interaction.appointment.service.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentListResponse(
	UUID appointmentId,
	String shopName,
	String designerNickname,
	String serviceName,
	LocalDateTime appointmentDate
) {
}
