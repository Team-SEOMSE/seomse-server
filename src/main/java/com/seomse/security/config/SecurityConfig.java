package com.seomse.security.config;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.seomse.security.jwt.JwtTokenProvider;
import com.seomse.security.jwt.filter.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
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

			.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			.authorizeHttpRequests(auth -> auth
				.requestMatchers(getPublicMatchers()).permitAll()
				.anyRequest().hasAnyAuthority("USER")
			)

			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
				.accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
			)

			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
		// .addFilterBefore(new JwtExceptionHandlerFilter(), JwtAuthenticationFilter.class);

		return http.build();
	}

	private RequestMatcher[] getPublicMatchers() {
		Builder pathPattern = withDefaults();
		return new RequestMatcher[] {
			pathPattern.matcher("/user/auth/**"),
			pathPattern.matcher("/h2-console/**"),
			pathPattern.matcher("/docs/**"),
			pathPattern.matcher("/v3/api-docs/**"),
			pathPattern.matcher("/error"),
			pathPattern.matcher("/health-check")
		};
	}
}
