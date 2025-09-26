package com.seomse.interaction.appointment.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.interaction.appointment.entity.AppointmentEntity;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
	Boolean existsByDesignerShopIdAndAppointmentDateAndAppointmentTime(UUID designerShop,
		LocalDate appointmentDate, LocalTime appointmentTime);
}
