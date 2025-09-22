package com.seomse.interaction.appointment.controller.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record NormalAppointmentCreateRequest(

	UUID shopId,

	UUID designerId,

	LocalDate appointmentDate,

	LocalTime appointmentTime,

	String serviceName
) implements AppointmentBaseRequest {
}
