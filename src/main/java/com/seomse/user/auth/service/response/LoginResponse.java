package com.seomse.user.auth.service.response;

import com.seomse.security.jwt.dto.JwtToken;

public record LoginResponse(JwtToken accessToken) {
}
