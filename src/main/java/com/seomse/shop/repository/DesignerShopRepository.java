package com.seomse.shop.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.shop.entity.DesignerShopEntity;

public interface DesignerShopRepository extends JpaRepository<DesignerShopEntity, UUID> {
}
