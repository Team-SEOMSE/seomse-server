package com.seomse.security.jwt.filter;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.seomse.security.SecurityConstants;
import com.seomse.security.jwt.JwtTokenProvider;
import com.seomse.security.jwt.exception.JwtTokenException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return Arrays.stream(SecurityConstants.PUBLIC_PATHS)
			.anyMatch(p -> path.startsWith(p.replace("/**", "")));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {

		String token = jwtTokenProvider.extractToken(request);

		if (token != null && !token.isBlank()) {
			try {
				if (jwtTokenProvider.isTokenExpired(token)) {
					throw new JwtTokenException("Expired JWT Token.");
				}
				Authentication authentication = jwtTokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException e) {
				SecurityContextHolder.clearContext();
				throw new JwtTokenException("Invalid JWT Token.");
			}
		}

		chain.doFilter(request, response);
	}
}
