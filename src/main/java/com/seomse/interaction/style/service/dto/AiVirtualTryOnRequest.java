package com.seomse.interaction.style.service.dto;

import com.seomse.user.client.enums.Gender;

public record AiVirtualTryOnRequest(String imageUrl, String targetHairstyle, String targetHairColor, Gender gender) {
}
