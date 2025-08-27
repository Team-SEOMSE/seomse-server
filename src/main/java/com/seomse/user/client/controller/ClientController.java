package com.seomse.user.client.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seomse.common.controller.ApiResponse;
import com.seomse.user.client.controller.request.UserProfileUpdateRequest;
import com.seomse.user.client.service.ClientService;
import com.seomse.user.client.service.response.UserProfileResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/user/client")
@RestController
public class ClientController {

	private final ClientService clientService;

	@GetMapping("/me")
	public ApiResponse<UserProfileResponse> getUserProfile() {
		return ApiResponse.ok(clientService.getUserProfile());
	}

	@PatchMapping("/me")
	public ApiResponse<UUID> updateUserProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
		return ApiResponse.ok(clientService.updateUserProfile(request.toServiceRequest()));
	}
}
