package com.seomse.user.client.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;

public final class ClientTestFactory {

	public static ClientEntity newClient(BCryptPasswordEncoder encoder, String email, String rawPassword,
		SnsType sns, Gender gender, Age age) {
		return new ClientEntity(email, encoder.encode(rawPassword), sns, gender, age);
	}
}