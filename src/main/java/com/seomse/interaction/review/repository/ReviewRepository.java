package com.seomse.interaction.review.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.interaction.review.entity.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
}
