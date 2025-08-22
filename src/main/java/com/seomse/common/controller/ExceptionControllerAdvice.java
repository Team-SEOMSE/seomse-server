package com.seomse.common.controller;

import static org.springframework.http.HttpStatus.*;

import java.io.IOException;
import java.net.BindException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.seomse.common.exception.dto.ExceptionResult;
import com.seomse.jwt.exception.JwtTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice {

	/** 예상치 못한 서버로직에러 발생시 처리 */
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> exception(Exception e) throws IOException {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(INTERNAL_SERVER_ERROR)
			.body(new ExceptionResult(INTERNAL_SERVER_ERROR.name(), e.getMessage()));
	}

	/** Validation Exception */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(BindException.class)
	public ResponseEntity<Object> bindException(BindException e) {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(BAD_REQUEST)
			.body(new ExceptionResult(BAD_REQUEST.name(), e.getMessage()));
	}

	/** Jwt Exception */
	@ResponseStatus(UNAUTHORIZED)
	@ExceptionHandler(JwtTokenException.class)
	public ResponseEntity<Object> JwtTokenException(JwtTokenException e) {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(UNAUTHORIZED)
			.body(new ExceptionResult(UNAUTHORIZED.name(), e.getMessage()));
	}

	/** 메서드에 전달된 인자가 부적절할 때 발생하는 예외 */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> NotFoundException(IllegalArgumentException e) {
		log.error(e.getMessage());

		return ResponseEntity.status(BAD_REQUEST)
			.body(new ExceptionResult(BAD_REQUEST.name(), e.getMessage()));
	}
}
