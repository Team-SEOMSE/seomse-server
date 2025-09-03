package com.seomse.interaction.review.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.appointment.repository.AppointmentRepository;
import com.seomse.interaction.review.controller.request.ReviewCreateRequest;
import com.seomse.interaction.review.entity.ReviewEntity;
import com.seomse.interaction.review.repository.ReviewQueryRepository;
import com.seomse.interaction.review.repository.ReviewRepository;
import com.seomse.interaction.review.service.response.ReviewListResponse;
import com.seomse.s3.service.S3Service;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.security.service.SecurityService;
import com.seomse.user.auth.enums.Role;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ReviewService {

	private final SecurityService securityService;
	private final S3Service s3Service;
	private final AppointmentRepository appointmentRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewQueryRepository reviewQueryRepository;

	public UUID createReview(ReviewCreateRequest request,
		MultipartFile reviewImage) {

		AppointmentEntity appointment = appointmentRepository.findById(request.appointmentId())
			.orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

		String s3Key = null;
		if (reviewImage != null && !reviewImage.isEmpty()) {
			final String S3_FOLDER = "review";
			try {
				s3Key = s3Service.upload(reviewImage, S3_FOLDER);
			} catch (IOException e) {
				throw new RuntimeException("Failed to upload review image.", e);
			}
		}

		ReviewEntity review = new ReviewEntity(appointment, request.reviewRating(), request.reviewContent(), s3Key);
		reviewRepository.save(review);

		return review.getId();
	}

	public List<ReviewListResponse> getReviewList() {
		LoginUserInfo loginUser = securityService.getCurrentLoginUserInfo();
		Role role = loginUser.role();

		// 디자이너, 점주
		switch (role) {
			case OWNER -> {
				return reviewQueryRepository.findReviewListByOwner(loginUser.userId());
			}
			case DESIGNER -> {
				return reviewQueryRepository.findReviewListByDesigner(loginUser.userId());
			}
			default -> throw new IllegalStateException("Unsupported role.");
		}
	}
}
