package com.seomse.security.jwt.dto;

import java.util.UUID;

import com.seomse.user.auth.enums.Role;

public record LoginUserInfo(UUID userId, Role role) {
}
