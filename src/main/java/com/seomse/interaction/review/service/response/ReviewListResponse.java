package com.seomse.interaction.review.service.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewListResponse(
	UUID reviewId,
	String reviewRating,
	String reviewContent,
	String reviewImage,
	String shopName,
	String designerNickName,
	String serviceName,
	LocalDateTime appointmentDate
) {
}
