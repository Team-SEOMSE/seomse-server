package com.seomse;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.seomse.security.feign.kakao.KakaoApiFeignCall;
import com.seomse.security.service.SecurityService;

import software.amazon.awssdk.services.s3.S3Client;

@ActiveProfiles("local")
@SpringBootTest
public abstract class IntegrationTestSupport {

	@MockitoBean
	protected SecurityService securityService;

	@MockitoBean
	protected KakaoApiFeignCall kakaoApiFeignCall;

	@MockitoBean
	protected S3Client s3Client;

}

