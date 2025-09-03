package com.seomse.s3.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@RequiredArgsConstructor
@Service
public class S3Service {

	private final S3Client s3Client;

	@Value("${s3.cloud-front}")
	private String cloudFrontDomain;

	@Value("${s3.bucket}")
	private String bucket;

	public String upload(MultipartFile file, String folder) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File is missing.");
		}

		String contentType = file.getContentType();
		if (!contentType.startsWith("image/")) {
			throw new IllegalArgumentException("Only image files are allowed.");
		}

		String extension = getExtension(file.getOriginalFilename());
		String fileName = UUID.randomUUID() + "." + extension;

		String key = Optional.ofNullable(folder)
			.filter(f -> !f.isBlank())
			.map(f -> f + "/" + fileName)
			.orElse(fileName);

		s3Client.putObject(
			builder -> builder.bucket(bucket).key(key)
				.contentType(file.getContentType()).contentDisposition("inline"),
			RequestBody.fromInputStream(file.getInputStream(), file.getSize())
		);

		return cloudFrontDomain + "/" + key;
	}

	private String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}
}
