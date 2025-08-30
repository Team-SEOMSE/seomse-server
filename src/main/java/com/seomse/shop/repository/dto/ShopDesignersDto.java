package com.seomse.shop.repository.dto;

import java.util.List;

import com.seomse.shop.entity.ShopEntity;

public record ShopDesignersDto(ShopEntity shop, List<DesignerInfoDto> designers) {
}
