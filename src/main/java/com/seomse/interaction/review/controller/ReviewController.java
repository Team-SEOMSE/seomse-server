package com.seomse.interaction.review.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.common.controller.ApiResponse;
import com.seomse.interaction.review.controller.request.ReviewCreateRequest;
import com.seomse.interaction.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/interaction/reviews")
@RestController
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<UUID> createReview(
		@Valid @RequestPart ReviewCreateRequest request,
		@RequestPart(required = false) MultipartFile reviewImage) {
		return ApiResponse.created(reviewService.createReview(request, reviewImage));
	}
}
