package com.seomse.user.auth.controller.request;

import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.request.LoginServiceRequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
	@NotBlank(message = "email is required.")
	@Email(message = "email format is invalid.")
	String email,

	@NotBlank(message = "password is required.")
	String password,

	@NotNull(message = "role is required.")
	Role role
) {

	public LoginServiceRequest toServiceRequest() {
		return new LoginServiceRequest(email, password, role);
	}
}
