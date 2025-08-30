package com.seomse.user.owner.entity;

public final class OwnerTestFactory {
	public static OwnerEntity newOwner(String email, String password) {
		return new OwnerEntity(email, password);
	}
}
