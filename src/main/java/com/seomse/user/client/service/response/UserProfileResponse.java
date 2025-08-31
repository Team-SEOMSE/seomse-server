package com.seomse.user.client.service.response;

import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;
import com.seomse.user.client.enums.SnsType;

public record UserProfileResponse(String email, String name, SnsType snsType, Gender gender, Age age) {
}
