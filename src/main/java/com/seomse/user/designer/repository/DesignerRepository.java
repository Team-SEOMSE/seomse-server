package com.seomse.user.designer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.user.designer.entity.DesignerEntity;

public interface DesignerRepository extends JpaRepository<DesignerEntity, UUID> {
}
