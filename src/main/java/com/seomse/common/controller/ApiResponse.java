package com.seomse.common.controller;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(int statusCode, T data) {

	public static <T> ApiResponse<T> of(HttpStatus status, T data) {
		return new ApiResponse<>(status.value(), data);
	}

	public static <T> ApiResponse<T> ok(T data) {
		return of(HttpStatus.OK, data);
	}

	public static <T> ApiResponse<T> created(T data) {
		return of(HttpStatus.CREATED, data);
	}
}
