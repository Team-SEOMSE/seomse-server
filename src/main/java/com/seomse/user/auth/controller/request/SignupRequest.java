package com.seomse.user.auth.controller.request;

import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.request.SignupServiceRequest;
import com.seomse.user.client.enums.SnsType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
	@NotBlank(message = "email is required.")
	@Email(message = "email format is invalid.")
	String email,

	@NotBlank(message = "password is required.")
	String password,

	@NotBlank(message = "name is required.")
	String name,

	@NotNull(message = "snsType is required.")
	SnsType snsType,

	@NotNull(message = "role is required.")
	Role role
) {

	public SignupServiceRequest toServiceRequest() {
		return new SignupServiceRequest(email, password, name, snsType, role);
	}
}
