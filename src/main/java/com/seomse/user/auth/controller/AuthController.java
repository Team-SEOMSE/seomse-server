package com.seomse.user.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.common.controller.ApiResponse;
import com.seomse.user.auth.controller.request.EmailCheckRequest;
import com.seomse.user.auth.controller.request.LoginRequest;
import com.seomse.user.auth.controller.request.OauthLoginRequest;
import com.seomse.user.auth.controller.request.SignupRequest;
import com.seomse.user.auth.service.AuthService;
import com.seomse.user.auth.service.response.EmailCheckResponse;
import com.seomse.user.auth.service.response.LoginResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/user/auth")
@RestController
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) throws JsonProcessingException {
		return ApiResponse.ok(authService.normalLogin(request.toServiceRequest()));
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public ApiResponse<UUID> signup(@Valid @RequestBody SignupRequest request) {
		return ApiResponse.created(authService.signup(request.toServiceRequest()));
	}

	@GetMapping("/check")
	public ApiResponse<EmailCheckResponse> checkEmail(@Valid EmailCheckRequest request) {
		EmailCheckResponse exists = authService.emailExists(request.toServiceRequest());
		return ApiResponse.ok(exists);
	}

	@PostMapping("/oauth/login")
	public ApiResponse<LoginResponse> oauthLogin(@Valid @RequestBody OauthLoginRequest request) throws
		JsonProcessingException {
		return ApiResponse.ok(authService.oauthLogin(request.toServiceRequest()));
	}
}
