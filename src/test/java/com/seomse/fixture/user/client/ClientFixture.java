package com.seomse.fixture.user.client;

import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.SnsType;

public class ClientFixture {
	public static ClientEntity createClient() {
		return new ClientEntity("user@email.com", "abc1234!", "김섬세",
			SnsType.NORMAL, null, null);
	}
}
