package com.seomse.security.feign.kakao.client;

import com.seomse.user.client.enums.SnsType;

public interface OAuthApiClient {
	SnsType oauthSnsType();

	String getEmail(String token);

	String getNickname(String token);

	String getToken(String kakaoClientId, String kakaoRedirectUri, String authorizationCode, String kakaoClientSecret);
}
