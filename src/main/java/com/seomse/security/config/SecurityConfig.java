package com.seomse.security.config;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.*;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.seomse.security.SecurityConstants;
import com.seomse.security.jwt.JwtTokenProvider;
import com.seomse.security.jwt.filter.JwtAuthenticationFilter;
import com.seomse.security.jwt.filter.JwtExceptionHandlerFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
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
			.cors(Customizer.withDefaults())

			.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(getPublicMatchers()).permitAll()
				.anyRequest().hasAnyAuthority("USER")
			)

			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new JwtExceptionHandlerFilter(), JwtAuthenticationFilter.class);

		return http.build();
	}

	private RequestMatcher[] getPublicMatchers() {
		return Arrays.stream(SecurityConstants.PUBLIC_PATHS)
			.map(path -> withDefaults().matcher(path))
			.toArray(RequestMatcher[]::new);
	}
}
