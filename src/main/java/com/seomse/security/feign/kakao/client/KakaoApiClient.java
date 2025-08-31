package com.seomse.security.feign.kakao.client;

import org.springframework.stereotype.Component;

import com.seomse.security.feign.kakao.KakaoApiFeignCall;
import com.seomse.security.feign.kakao.KakaoAuthFeignCall;
import com.seomse.user.client.enums.SnsType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthApiClient {

	private final KakaoApiFeignCall kakaoApiFeignCall;
	private final KakaoAuthFeignCall kakaoAuthFeignCall;

	@Override
	public SnsType oauthSnsType() {
		return SnsType.KAKAO;
	}

	@Override
	public String getEmail(String accessToken) {
		// 이메일은 권한이 없기 때문에 ID로 대체
		Long id = kakaoApiFeignCall.getUserInfo("Bearer " + accessToken).getId();
		return id + "@daum.net";
	}

	@Override
	public String getToken(String kakaoClientId, String kakaoRedirectUri, String authorizationCode,
		String kakaoClientSecret) {
		return kakaoAuthFeignCall.getToken("authorization_code", kakaoClientId, kakaoRedirectUri, authorizationCode,
			kakaoClientSecret).accessToken();
	}
}
