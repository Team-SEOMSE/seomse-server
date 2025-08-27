package com.seomse.user.auth.controller.request;

import com.seomse.user.auth.enums.Role;
import com.seomse.user.auth.service.request.EmailCheckServiceRequest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmailCheckRequest(
	@NotBlank(message = "email is required.")
	@Email(message = "email format is invalid.")
	String email,

	@NotNull(message = "role is required.")
	Role role
) {
	public EmailCheckServiceRequest toServiceRequest() {
		return new EmailCheckServiceRequest(email, role);
	}
}
