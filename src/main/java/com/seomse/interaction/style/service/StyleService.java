package com.seomse.interaction.style.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.seomse.common.util.ByteArrayMultipartFile;
import com.seomse.interaction.style.client.AiApiClient;
import com.seomse.interaction.style.entity.StyleAnalysisEntity;
import com.seomse.interaction.style.repository.StyleAnalysisRepository;
import com.seomse.interaction.style.service.dto.AiVirtualTryOnRequest;
import com.seomse.interaction.style.service.dto.StyleAnalysisRequest;
import com.seomse.interaction.style.service.dto.StyleAnalysisResponse;
import com.seomse.interaction.style.service.dto.VirtualTryOnResponse;
import com.seomse.interaction.style.service.request.VirtualTryOnServiceRequest;
import com.seomse.s3.service.S3Service;
import com.seomse.security.jwt.dto.LoginUserInfo;
import com.seomse.security.service.SecurityService;
import com.seomse.user.client.entity.ClientEntity;
import com.seomse.user.client.repository.ClientRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class StyleService {

	private final AiApiClient aiApiClient;
	private final SecurityService securityService;
	private final S3Service s3Service;

	private final StyleAnalysisRepository styleAnalysisRepository;
	private final ClientRepository clientRepository;

	public StyleAnalysisResponse callAnalyzeStyle(MultipartFile image) throws IOException {

		LoginUserInfo loginUser = securityService.getCurrentLoginUserInfo();

		ClientEntity client = clientRepository.findById(loginUser.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));

		final String S3_FOLDER = "style";
		final String s3Key = s3Service.upload(image, S3_FOLDER);

		StyleAnalysisRequest requestBody = new StyleAnalysisRequest(s3Key, client.getGender().toString(),
			client.getAge().toString());

		StyleAnalysisResponse response = aiApiClient.analyzeStyle(requestBody);

		StyleAnalysisEntity styleAnalysisEntity = new StyleAnalysisEntity(client, s3Key, response);
		styleAnalysisRepository.save(styleAnalysisEntity);

		return response;
	}

	public VirtualTryOnResponse callVirtualTryOn(VirtualTryOnServiceRequest request) throws IOException {

		StyleAnalysisEntity analysisEntity = styleAnalysisRepository.findByImage(request.imageUrl())
			.orElseThrow(() -> new IllegalArgumentException("image not found"));

		LoginUserInfo loginUser = securityService.getCurrentLoginUserInfo();
		ClientEntity client = clientRepository.findById(loginUser.userId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));

		AiVirtualTryOnRequest aiRequest = new AiVirtualTryOnRequest(request.imageUrl(), request.targetHairstyle(),
			request.targetHairColor(), client.getGender());

		byte[] generatedImageBytes = aiApiClient.virtualTryOn(aiRequest);

		MultipartFile generatedFile = new ByteArrayMultipartFile(
			generatedImageBytes,
			"file",
			UUID.randomUUID() + ".png",
			"image/png"
		);

		final String S3_FOLDER = "style";
		final String resultImageUrl = s3Service.upload(generatedFile, S3_FOLDER);

		analysisEntity.updateVirtualTryOnImage(resultImageUrl);

		return new VirtualTryOnResponse(resultImageUrl);
	}
}
