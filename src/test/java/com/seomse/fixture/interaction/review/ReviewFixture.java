package com.seomse.fixture.interaction.review;

import com.seomse.interaction.appointment.entity.AppointmentEntity;
import com.seomse.interaction.review.entity.ReviewEntity;

public class ReviewFixture {
	public static ReviewEntity createReviewEntity(AppointmentEntity appointment) {
		return new ReviewEntity(appointment, "5", "reviewContent", "/img.png");
	}
}
