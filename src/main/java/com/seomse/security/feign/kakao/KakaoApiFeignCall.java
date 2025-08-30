package com.seomse.security.feign.kakao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.seomse.security.feign.kakao.response.KakaoUserInfoResponse;

@Component
@FeignClient(name = "kakaoApi", url = "https://kapi.kakao.com")
public interface KakaoApiFeignCall {

	@GetMapping("/v2/user/me")
	KakaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String accessToken);
}
