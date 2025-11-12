package com.seomse.interaction.style.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.seomse.IntegrationTestSupport;
import com.seomse.interaction.style.entity.StyleAnalysisEntity;
import com.seomse.interaction.style.repository.StyleAnalysisRepository;
import com.seomse.interaction.style.service.dto.StyleAnalysisRequest;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.user.auth.enums.Role;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;
import com.seomse.user.client.repository.ClientRepository;

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

		ClassPathResource resource = new ClassPathResource("test.jpg");
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
}