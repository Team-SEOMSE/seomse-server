package com.seomse.user.auth.service.dto;

import java.util.UUID;

public record UserAuthInfo(UUID id, String encryptedPassword) {
}
