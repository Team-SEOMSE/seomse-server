package com.seomse.user.designer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.user.designer.entity.DesignerEntity;

import java.util.Optional;

public interface DesignerRepository extends JpaRepository<DesignerEntity, UUID> {
    Optional<DesignerEntity> findByEmail(String email);
}
