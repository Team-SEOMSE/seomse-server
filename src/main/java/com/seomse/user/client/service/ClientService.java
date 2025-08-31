package com.seomse.user.client.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.security.service.SecurityService;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.repository.ClientRepository;
import com.seomse.user.client.service.request.UserProfileUpdateServiceRequest;
import com.seomse.user.client.service.response.UserProfileResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ClientService {

	private final ClientRepository clientRepository;
	private final SecurityService securityService;

	public UserProfileResponse getUserProfile() {
		LoginUserInfo userInfo = securityService.getCurrentLoginUserInfo();

		ClientEntity client = clientRepository.findById(userInfo.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));

		return new UserProfileResponse(client.getEmail(), client.getName(), client.getSnsType(), client.getGender(),
			client.getAge());
	}

	public UUID updateUserProfile(UserProfileUpdateServiceRequest request) {
		LoginUserInfo userInfo = securityService.getCurrentLoginUserInfo();

		ClientEntity client = clientRepository.findById(userInfo.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));

		ClientEntity updatedClient = client.updateProfile(request.gender(), request.age());
		return updatedClient.getId();
	}
}
