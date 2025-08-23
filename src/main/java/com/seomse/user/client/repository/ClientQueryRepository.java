package com.seomse.user.client.repository;

import static com.seomse.user.client.entity.QClientEntity.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seomse.user.client.entity.ClientEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ClientQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<ClientEntity> findByEmail(String email) {
		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(clientEntity)
				.where(clientEntity.email.eq(email))
				.fetchOne()
		);
	}
}
