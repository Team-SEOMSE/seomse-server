package com.seomse.user.auth.service.request;

import com.seomse.user.client.enums.SnsType;

public record OauthLoginServiceRequest(String code, SnsType snsType) {
}
