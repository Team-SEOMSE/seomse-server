package com.seomse.user.client.controller.request;

import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.service.request.UserProfileUpdateServiceRequest;

import jakarta.validation.constraints.NotNull;

public record UserProfileUpdateRequest(
	@NotNull(message = "gender is required.")
	Gender gender,

	@NotNull(message = "age is required.")
	Age age
) {
	public UserProfileUpdateServiceRequest toServiceRequest() {
		return new UserProfileUpdateServiceRequest(gender, age);
	}
}
