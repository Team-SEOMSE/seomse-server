package com.seomse.security.jwt.filter;

import static org.springframework.http.HttpStatus.*;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seomse.common.exception.dto.ExceptionResult;
import com.seomse.security.jwt.exception.JwtTokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (JwtTokenException e) {
			setErrorResponse(response, e);
		}
	}

	private void setErrorResponse(HttpServletResponse response, Exception exception) {
		ObjectMapper objectMapper = new ObjectMapper();
		response.setStatus(UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.addHeader("Content-Type", "application/json; charset=UTF-8");

		try {
			response.getWriter()
				.write(objectMapper.writeValueAsString(
					new ExceptionResult(UNAUTHORIZED.name(), exception.getMessage())));
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}
