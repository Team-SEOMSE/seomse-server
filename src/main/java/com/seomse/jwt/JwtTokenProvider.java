package com.seomse.jwt;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seomse.jwt.dto.LoginUserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {

	private final ObjectMapper objectMapper;
	private final Key signingKey;

	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	public String generateToken(String subject, Date expirationDate) {
		return Jwts.builder()
			.setSubject(subject)
			.setExpiration(expirationDate)
			.claim("UUID", UUID.randomUUID().toString())
			.signWith(signingKey, SignatureAlgorithm.HS512)
			.compact();
	}

	public String extractToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7).trim();
		}
		return null;
	}

	public boolean isTokenExpired(String token) {
		return parseClaims(token).getExpiration().before(new Date());
	}

	public Authentication getAuthentication(String token) throws JsonProcessingException {
		String userPk = getUserPrimaryKey(token);
		LoginUserInfo loginUserInfo = objectMapper.readValue(userPk, LoginUserInfo.class);
		return new UsernamePasswordAuthenticationToken(loginUserInfo, "",
			Collections.singletonList(new SimpleGrantedAuthority("USER")));
	}

	public String getUserPrimaryKey(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(signingKey)
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(signingKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}
