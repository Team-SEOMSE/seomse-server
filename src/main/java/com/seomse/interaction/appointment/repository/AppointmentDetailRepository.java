package com.seomse.interaction.appointment.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.interaction.appointment.entity.AppointmentDetailEntity;

public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetailEntity, UUID> {
	Optional<AppointmentDetailEntity> findByAppointmentId(UUID appointmentId);
}
