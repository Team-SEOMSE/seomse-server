package com.seomse.interaction.style.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.seomse.interaction.style.service.dto.StyleAnalysisRequest;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiApiClient {

	private final WebClient webClient;

	public StyleAnalysisResponse analyzeStyle(StyleAnalysisRequest requestBody) {
		return webClient.post()
			.uri("/analyze-style")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.bodyValue(requestBody)
			.retrieve()
			.bodyToMono(StyleAnalysisResponse.class)
			.block();
	}
}
