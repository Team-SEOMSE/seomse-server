package com.seomse.interaction.style.service.dto;

public record StyleAnalysisResponse(
	AnalysisData analysis,
	RecommendationsData recommendations
) {

	public record AnalysisData(
		String faceShape,
		String personalColor
	) {
	}

	public record RecommendationsData(
		RecommendationDetail hairstyle,
		RecommendationDetail hairColor
	) {
	}

	public record RecommendationDetail(
		String name,
		String reason
	) {
	}
}
