package com.seomse.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.seomse.jwt.JwtTokenProvider;
import com.seomse.jwt.filter.JwtAuthenticationFilter;
import com.seomse.jwt.filter.JwtExceptionHandlerFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)

			.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(getPublicMatchers()).permitAll()
				.anyRequest().hasAnyAuthority("USER")
			)

			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new JwtExceptionHandlerFilter(), JwtAuthenticationFilter.class);

		return http.build();
	}

	private RequestMatcher[] getPublicMatchers() {
		return new RequestMatcher[] {
			new AntPathRequestMatcher("/api/oauth/**"),
			new AntPathRequestMatcher("/h2-console/**"),
			new AntPathRequestMatcher("/docs/**"),
			new AntPathRequestMatcher("/v3/api-docs/**"),
			new AntPathRequestMatcher("/error")
		};
	}
}
