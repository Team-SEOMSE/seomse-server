package com.seomse.user.auth.service.request;

import com.seomse.user.auth.enums.Role;

public record EmailCheckServiceRequest(String email, Role role) {
}
