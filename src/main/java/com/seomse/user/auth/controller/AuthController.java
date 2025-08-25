package com.seomse.user.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.seomse.common.controller.ApiResponse;
import com.seomse.user.auth.controller.request.LoginRequest;
import com.seomse.user.auth.service.AuthService;
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

}
