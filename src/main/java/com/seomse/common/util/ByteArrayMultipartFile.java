package com.seomse.common.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ByteArrayMultipartFile implements MultipartFile {
	private final byte[] content;
	private final String name;
	private final String originalFilename;
	private final String contentType;

	@Override
	public boolean isEmpty() {
		return content == null || content.length == 0;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return content;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		try (FileOutputStream fos = new FileOutputStream(dest)) {
			fos.write(content);
		}
	}
}