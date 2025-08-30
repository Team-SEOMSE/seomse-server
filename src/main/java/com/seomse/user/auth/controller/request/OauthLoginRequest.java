package com.seomse.user.auth.controller.request;

import com.seomse.user.auth.service.request.OauthLoginServiceRequest;
import com.seomse.user.client.enums.SnsType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OauthLoginRequest(
	@NotBlank(message = "code is required.")
	String code,

	@NotNull(message = "snsType is required.")
	SnsType snsType

) {

	public OauthLoginServiceRequest toServiceRequest() {
		return new OauthLoginServiceRequest(code, snsType);
	}
}
