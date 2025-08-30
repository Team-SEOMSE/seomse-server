package com.seomse.user.client.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.user.client.entity.ClientEntity;

public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
	Optional<ClientEntity> findByEmail(String email);
}
