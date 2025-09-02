package com.seomse.user.auth.service.dto;

import com.seomse.user.client.entity.ClientEntity;

public record ClientAndStatus(ClientEntity client, boolean isNew) {
}
