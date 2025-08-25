package com.seomse.user.auth.service.request;

import com.seomse.user.auth.enums.Role;

public record LoginServiceRequest(String email, String password, Role role) {
}
