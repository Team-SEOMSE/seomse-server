package com.seomse.security;

public final class SecurityConstants {

	private SecurityConstants() {
	}

	public static final String[] PUBLIC_PATHS = {
		"/user/auth/**",
		"/h2-console/**",
		"/docs/**",
		"/v3/api-docs/**",
		"/error",
		"/health-check"
	};
}

