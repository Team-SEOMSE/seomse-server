package com.seomse.interaction.review.controller.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
	@NotNull(message = "appointmentId is required.")
	UUID appointmentId,

	@NotNull(message = "reviewRating is required.")
	String reviewRating,

	@NotBlank(message = "reviewContent must not be blank.")
	String reviewContent
) {
}
