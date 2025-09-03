package com.seomse.shop.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.shop.entity.ShopEntity;
import com.seomse.shop.enums.Type;

public interface ShopRepository extends JpaRepository<ShopEntity, UUID> {
	List<ShopEntity> findAllByType(Type type);
}
