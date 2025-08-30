package com.seomse.s3.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.common.controller.ApiResponse;
import com.seomse.s3.service.S3Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/s3")
@RestController
public class S3Controller {

	private final S3Service s3Service;

	@PostMapping("/upload")
	public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
		String key = s3Service.upload(file);
		return ApiResponse.ok(key);
	}
}
