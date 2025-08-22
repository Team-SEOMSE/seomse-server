package com.seomse.jwt.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.seomse.jwt.JwtTokenProvider;
import com.seomse.jwt.exception.JwtTokenException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final List<String> EXCLUDE_PATHS = Arrays.asList(
		"/api/oauth",
		"/h2-console",
		"/docs",
		"/v3/api-docs"
	);
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			String token = jwtTokenProvider.extractToken(request);
			if (token != null && !jwtTokenProvider.isTokenExpired(token)) {
				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				throw new JwtTokenException("Invalid or expired JWT Token.");
			}
		} catch (JwtException | IllegalArgumentException e) {
			throw new JwtTokenException("Invalid JWT Token.");
		}

		filterChain.doFilter(request, response);
	}
}
