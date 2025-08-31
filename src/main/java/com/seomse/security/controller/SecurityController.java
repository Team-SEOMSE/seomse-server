package com.seomse.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seomse.common.controller.ApiResponse;
import com.seomse.security.service.SecurityService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/security")
@RestController
public class SecurityController {

	private final SecurityService securityService;

	@GetMapping("/verify")
	public ApiResponse<String> verify() {
		securityService.getCurrentLoginUserInfo();
		return ApiResponse.ok("OK");
	}
}
