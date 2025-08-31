package com.seomse.interaction.appointment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.interaction.appointment.entity.AppointmentEntity;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
}
