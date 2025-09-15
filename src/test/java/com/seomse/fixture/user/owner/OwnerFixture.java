package com.seomse.fixture.user.owner;

import com.seomse.user.owner.entity.OwnerEntity;

public class OwnerFixture {
	public static OwnerEntity createOwnerEntity() {
		return new OwnerEntity("user@email.com", "abc1234!");
	}
}
