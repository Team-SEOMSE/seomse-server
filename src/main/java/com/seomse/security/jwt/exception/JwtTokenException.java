package com.seomse.security.jwt.exception;

import lombok.Getter;

@Getter
public class JwtTokenException extends RuntimeException {
	public JwtTokenException(String message) {
		super(message);
	}
}
