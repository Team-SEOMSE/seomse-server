package com.seomse.user.owner.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.user.owner.entity.OwnerEntity;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<OwnerEntity, UUID> {
    Optional<OwnerEntity> findByEmail(String email);
}
