package com.seomse.security.feign.kakao.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoResponse {

	private Long id;

	@JsonProperty("connected_at")
	private String connectedAt;

	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	@JsonProperty("properties")
	private Properties properties;

	@Getter
	@NoArgsConstructor
	public static class Properties {

		@JsonProperty("nickname")
		private String nickname;

		public Properties(String nickname) {
			this.nickname = nickname;
		}
	}

	public static class KakaoAccount {

		@JsonProperty("has_email")
		private boolean hasEmail;

		@JsonProperty("email_needs_agreement")
		private boolean emailNeedsAgreement;

		@JsonProperty("is_email_valid")
		private boolean isEmailValid;

		@JsonProperty("is_email_verified")
		private boolean isEmailVerified;

		@JsonProperty("email")
		private String email;

		public KakaoAccount(boolean hasEmail, boolean emailNeedsAgreement, boolean isEmailValid,
			boolean isEmailVerified, String email) {
			this.hasEmail = hasEmail;
			this.emailNeedsAgreement = emailNeedsAgreement;
			this.isEmailValid = isEmailValid;
			this.isEmailVerified = isEmailVerified;
			this.email = email;
		}

	}

	public String getNickname() {
		return properties.nickname;
	}

	public KakaoUserInfoResponse(long id, String connectedAt, KakaoAccount kakaoAccount, Properties properties) {
		this.id = id;
		this.connectedAt = connectedAt;
		this.kakaoAccount = kakaoAccount;
		this.properties = properties;
	}
}