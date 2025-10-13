package com.seomse.interaction.appointment.service.response;

import java.time.LocalTime;

public record AppointmentTimeListResponse(
	LocalTime appointmentTime
) {
}
