package com.seomse.s3.service;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import com.seomse.IntegrationTestSupport;

class S3ServiceTest extends IntegrationTestSupport {

	@Autowired
	private S3Service s3Service;

	@DisplayName("s3에 이미지를 업로드한다.")
	@Test
	void upload() throws IOException {
		// given
		String fileName = "test-image.png";
		String contentType = "image/png";
		String fileContent = "test image content";

		MockMultipartFile mockFile = new MockMultipartFile(
			"file",
			fileName,
			contentType,
			fileContent.getBytes()
		);

		// when
		String uploadedKey = s3Service.upload(mockFile, null);

		// then
		Assertions.assertThat(uploadedKey).isNotNull();
		Assertions.assertThat(uploadedKey).endsWith(".png");

	}

}