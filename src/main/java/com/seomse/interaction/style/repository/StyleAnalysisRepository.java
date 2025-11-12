package com.seomse.interaction.style.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.interaction.style.entity.StyleAnalysisEntity;

public interface StyleAnalysisRepository extends JpaRepository<StyleAnalysisEntity, UUID> {
}
