package com.seomse.jwt.dto;

public record JwtToken(
	String accessToken,
	String refreshToken,
	String grantType,
	Long expiresIn
) {
}