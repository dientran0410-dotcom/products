package com.fsa.franchise.product_service.service;

import java.util.List;
import java.util.UUID;

import com.fsa.franchise.product_service.dto.response.ProductVariantDto;

public interface ProductVariantService {
    List<ProductVariantDto> getVariantsByIds(List<UUID> ids);
}