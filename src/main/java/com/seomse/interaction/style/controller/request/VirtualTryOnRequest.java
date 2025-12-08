package com.seomse.interaction.style.controller.request;

import com.seomse.interaction.style.service.request.VirtualTryOnServiceRequest;

import jakarta.validation.constraints.NotBlank;

public record VirtualTryOnRequest(
	@NotBlank(message = "imageUrl is required.")
	String imageUrl,

	@NotBlank(message = "targetHairstyle is required.")
	String targetHairstyle,

	@NotBlank(message = "targetHairColor is required.")
	String targetHairColor

) {
	public VirtualTryOnServiceRequest toServiceRequest() {
		return new VirtualTryOnServiceRequest(imageUrl, targetHairstyle, targetHairColor);
	}
}
