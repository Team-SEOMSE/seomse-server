package com.seomse.interaction.style.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.common.controller.ApiResponse;
import com.seomse.interaction.style.controller.request.VirtualTryOnRequest;
import com.seomse.interaction.style.service.StyleService;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;
import com.seomse.interaction.style.service.dto.VirtualTryOnResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/interaction/styles")
@RestController
public class StyleController {

	private final StyleService styleService;

	@PostMapping("/analysis")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<StyleAnalysisResponse> analyzeStyle(@RequestPart MultipartFile image) throws IOException {
		return ApiResponse.created(styleService.callAnalyzeStyle(image));
	}

	@PostMapping("/virtual-try-on")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<VirtualTryOnResponse> virtualTryOn(@RequestBody VirtualTryOnRequest request) throws IOException {
		return ApiResponse.created(styleService.callVirtualTryOn(request.toServiceRequest()));
	}
}
