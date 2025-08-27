package com.seomse.user.client.service.request;

import com.seomse.user.client.enums.Age;
import com.seomse.user.client.enums.Gender;

public record UserProfileUpdateServiceRequest(Gender gender, Age age) {
}
