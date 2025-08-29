package com.seomse.designerShop.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seomse.designerShop.entity.DesignerShopEntity;

public interface DesignerShopRepository extends JpaRepository<DesignerShopEntity, UUID> {
}
