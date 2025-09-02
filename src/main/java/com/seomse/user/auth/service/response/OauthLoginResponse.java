package com.seomse.user.auth.service.response;

public record OauthLoginResponse(String accessToken, boolean isNew) {
}
