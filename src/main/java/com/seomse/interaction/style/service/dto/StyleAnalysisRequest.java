package com.seomse.interaction.style.service.dto;

public record StyleAnalysisRequest(
	String imageUrl,
	String gender,
	String ageGroup
) {
}
