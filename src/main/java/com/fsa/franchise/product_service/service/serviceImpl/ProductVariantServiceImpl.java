package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.response.ProductVariantDto;
import com.fsa.franchise.product_service.entity.ProductVariant;
import com.fsa.franchise.product_service.repository.ProductVariantRepository;
import com.fsa.franchise.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDto> getVariantsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<ProductVariant> variants = productVariantRepository.findByIdsWithProduct(ids);

        return variants.stream().map(v -> {
            String fullName = v.getProduct().getName();
            if (!v.isDefault() && v.getName() != null && !v.getName().isEmpty()) {
                fullName += " (" + v.getName() + ")";
            }

            return ProductVariantDto.builder()
                    .id(v.getId())
                    .sku(v.getSku())
                    .name(fullName)
                    .price(v.getPrice())
                    .status(v.getStatus())

                    .build();
        }).collect(Collectors.toList());
    }
}