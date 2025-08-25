package com.seomse.common.controller;

import static org.springframework.http.HttpStatus.*;

import java.io.IOException;
import java.net.BindException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.seomse.common.exception.dto.ExceptionResult;
import com.seomse.security.jwt.exception.JwtTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionControllerAdvice {

	/** Handles unexpected server-side errors (Exception). */
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> exception(Exception e) throws IOException {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(INTERNAL_SERVER_ERROR)
			.body(new ExceptionResult(INTERNAL_SERVER_ERROR.name(), e.getMessage()));
	}

	/** Handles request binding/validation errors (BindException). */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(BindException.class)
	public ResponseEntity<Object> bindException(BindException e) {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(BAD_REQUEST)
			.body(new ExceptionResult(BAD_REQUEST.name(), e.getMessage()));
	}

	/** Handles JWT authentication errors (JwtTokenException). */
	@ResponseStatus(UNAUTHORIZED)
	@ExceptionHandler(JwtTokenException.class)
	public ResponseEntity<Object> JwtTokenException(JwtTokenException e) {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(UNAUTHORIZED)
			.body(new ExceptionResult(UNAUTHORIZED.name(), e.getMessage()));
	}

	/** Handles invalid request arguments (IllegalArgumentException). */
	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> IllegalArgumentException(IllegalArgumentException e) {
		log.error(e.getMessage());

		return ResponseEntity.status(BAD_REQUEST)
			.body(new ExceptionResult(BAD_REQUEST.name(), e.getMessage()));
	}

	/** Handles unsupported HTTP methods (405 Method Not Allowed). */
	@ResponseStatus(METHOD_NOT_ALLOWED)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Object> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
		log.error(e.getMessage());

		return ResponseEntity.status(METHOD_NOT_ALLOWED)
			.body(new ExceptionResult(METHOD_NOT_ALLOWED.name(), e.getMessage()));
	}

	/** Static resource not found (404). */
	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Object> staticResourceNotFound(NoResourceFoundException e) {
		log.error(e.getMessage(), e);
		
		return ResponseEntity.status(NOT_FOUND)
			.body(new ExceptionResult(NOT_FOUND.name(), e.getMessage()));
	}

	/** Endpoint not found (404). */
	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<Object> noHandlerFound(NoHandlerFoundException e) {
		log.error(e.getMessage(), e);

		return ResponseEntity.status(NOT_FOUND)
			.body(new ExceptionResult(NOT_FOUND.name(), e.getMessage()));
	}
}
