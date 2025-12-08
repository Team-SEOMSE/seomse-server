package com.seomse.interaction.style.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.seomse.IntegrationTestSupport;
import com.seomse.interaction.style.entity.StyleAnalysisEntity;
import com.seomse.interaction.style.repository.StyleAnalysisRepository;
import com.seomse.interaction.style.service.dto.AiVirtualTryOnRequest;
import com.seomse.interaction.style.service.dto.StyleAnalysisRequest;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;
import com.seomse.interaction.style.service.dto.VirtualTryOnResponse;
import com.seomse.interaction.style.service.request.VirtualTryOnServiceRequest;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.repository.ClientRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class StyleServiceTest extends IntegrationTestSupport {

	@Autowired
	private StyleService styleService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private StyleAnalysisRepository styleAnalysisRepository;

	@AfterEach
	void tearDown() {
		styleAnalysisRepository.deleteAll();
		clientRepository.deleteAll();
	}

	@DisplayName("성공: 클라이언트가 이미지를 업로드하면 스타일 분석을 수행한다.")
	@Test
	void callAnalyzeStyle() throws IOException {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;
		Gender gender = Gender.MALE;
		Age age = Age.TWENTIES;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, gender,
			age);

		clientRepository.save(client);

		BDDMockito.given(securityService.getCurrentLoginUserInfo())
			.willReturn(new LoginUserInfo(client.getId(), Role.CLIENT));

		MockMultipartFile image = new MockMultipartFile(
			"image",
			"test.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"image".getBytes()
		);

		StyleAnalysisResponse fakeFastApiResponse = new StyleAnalysisResponse(
			new StyleAnalysisResponse.AnalysisData("계란형", "가을 웜톤"),
			new StyleAnalysisResponse.RecommendationsData(
				new StyleAnalysisResponse.RecommendationDetail("댄디컷", "이유..."),
				new StyleAnalysisResponse.RecommendationDetail("애쉬 브라운", "이유...")
			)
		);

		BDDMockito.given(aiApiClient.analyzeStyle(any(StyleAnalysisRequest.class)))
			.willReturn(fakeFastApiResponse);

		// when
		StyleAnalysisResponse response = styleService.callAnalyzeStyle(image);

		// then
		assertThat(response).isNotNull();
		assertThat(response.analysis()).isNotNull();
		assertThat(response.analysis().faceShape()).isNotBlank();
		assertThat(response.recommendations().hairstyle().name()).isNotBlank();

		List<StyleAnalysisEntity> analyses = styleAnalysisRepository.findAll();
		assertThat(analyses).hasSize(1);

		StyleAnalysisEntity savedAnalysis = analyses.get(0);
		assertThat(savedAnalysis.getClient().getId()).isEqualTo(client.getId());

		assertThat(savedAnalysis.getImage()).isNotNull();
	}

	@DisplayName("성공: 기존 분석 이미지를 기반으로 가상 체험을 요청하면 결과 이미지 URL을 반환한다")
	@Test
	void callVirtualTryOn() throws IOException {
		// given
		String email = "user@email.com";
		String password = "abc1234!";
		String name = "김섬세";
		SnsType snsType = SnsType.NORMAL;
		Gender gender = Gender.MALE;
		Age age = Age.TWENTIES;

		ClientEntity client = new ClientEntity(email, bCryptPasswordEncoder.encode(password), name, snsType, gender,
			age);

		clientRepository.save(client);

		BDDMockito.given(securityService.getCurrentLoginUserInfo())
			.willReturn(new LoginUserInfo(client.getId(), Role.CLIENT));

		String originalImageUrl = "https://test.cloudfront.net/style/test.png";
		StyleAnalysisResponse dummyResponse = new StyleAnalysisResponse(
			new StyleAnalysisResponse.AnalysisData("계란형", "가을 웜톤"),
			new StyleAnalysisResponse.RecommendationsData(
				new StyleAnalysisResponse.RecommendationDetail("댄디컷", "이유..."),
				new StyleAnalysisResponse.RecommendationDetail("애쉬 브라운", "이유...")
			)
		);

		StyleAnalysisEntity analysisEntity = new StyleAnalysisEntity(client, originalImageUrl, dummyResponse);
		styleAnalysisRepository.save(analysisEntity);

		String targetHairstyle = "댄디컷";
		String targetHairColor = "애쉬 브라운";
		VirtualTryOnServiceRequest request = new VirtualTryOnServiceRequest(originalImageUrl, targetHairstyle,
			targetHairColor);

		byte[] fakeGeneratedImage = "fake-image-bytes".getBytes();
		BDDMockito.given(aiApiClient.virtualTryOn(any(AiVirtualTryOnRequest.class)))
			.willReturn(fakeGeneratedImage);

		BDDMockito.given(s3Client.putObject(any(Consumer.class), any(RequestBody.class)))
			.willReturn(PutObjectResponse.builder().build());

		// when
		VirtualTryOnResponse response = styleService.callVirtualTryOn(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.generatedImageUrl()).isNotNull();
		assertThat(response.generatedImageUrl()).contains("/style/");
		assertThat(response.generatedImageUrl()).endsWith(".png");

		StyleAnalysisEntity updatedEntity = styleAnalysisRepository.findById(analysisEntity.getId()).orElseThrow();
		assertThat(updatedEntity.getVirtualTryOnImage()).isEqualTo(response.generatedImageUrl());
		assertThat(updatedEntity.getImage()).isEqualTo(originalImageUrl);
	}
}