package com.seomse.user.auth.service.request;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.SnsType;

public record SignupServiceRequest(String email, String password, String name, SnsType snsType, Role role) {

	public ClientEntity toClientEntity(BCryptPasswordEncoder bCryptPasswordEncoder) {
		return new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, null, null);
	}
}
