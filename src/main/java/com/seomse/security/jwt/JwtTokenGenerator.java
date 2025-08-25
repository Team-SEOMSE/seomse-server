package com.seomse.security.jwt;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seomse.security.jwt.dto.JwtToken;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.security.jwt.exception.JwtTokenException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtTokenGenerator {

	private static final String BEARER_TYPE = "Bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;  // 60 minutes
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 15;  // 15 days

	private final ObjectMapper objectMapper;
	private final JwtTokenProvider jwtTokenProvider;

	public JwtToken generate(LoginUserInfo loginUserInfo) throws JsonProcessingException {
		String subject = objectMapper.writeValueAsString(loginUserInfo);
		Date accessTokenExpiration = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME);
		Date refreshTokenExpiration = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME);

		String accessToken = jwtTokenProvider.generateToken(subject, accessTokenExpiration);
		String refreshToken = jwtTokenProvider.generateToken(subject, refreshTokenExpiration);

		return new JwtToken(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000);
	}

	public JwtToken refreshJwtToken(String refreshToken) {
		if (refreshToken == null) {
			throw new JwtTokenException("Refresh token is null.");
		}

		try {
			if (jwtTokenProvider.isTokenExpired(refreshToken)) {
				throw new JwtTokenException("Invalid or expired refresh token.");
			}

			String subject = jwtTokenProvider.getUserPrimaryKey(refreshToken);
			Date accessTokenExpiration = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME);
			Date refreshTokenExpiration = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME);

			String newAccessToken = jwtTokenProvider.generateToken(subject, accessTokenExpiration);
			String newRefreshToken = jwtTokenProvider.generateToken(subject, refreshTokenExpiration);

			return new JwtToken(newAccessToken, newRefreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000);
		} catch (ExpiredJwtException e) {
			throw new JwtTokenException("Expired refresh token.");
		} catch (JwtException | IllegalArgumentException e) {
			throw new JwtTokenException("Failed to refresh token.");
		}
	}
}
