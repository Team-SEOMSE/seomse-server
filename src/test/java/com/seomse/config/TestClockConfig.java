package com.seomse.config;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {
	@Bean
	@Primary
	public Clock fixedClock() {
		return Clock.fixed(
			ZonedDateTime.of(2025, 9, 17, 12, 0, 0, 0,
					ZoneId.of("Asia/Seoul"))
				.toInstant(),
			ZoneId.of("Asia/Seoul")
		);

	}
}
