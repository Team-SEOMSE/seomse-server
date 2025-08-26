package com.seomse.user.owner.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.user.owner.entity.OwnerEntity;

public interface OwnerRepository extends JpaRepository<OwnerEntity, UUID> {
}
