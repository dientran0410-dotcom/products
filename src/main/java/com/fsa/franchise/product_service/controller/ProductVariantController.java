package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.response.ProductVariantDto;
import com.fsa.franchise.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping("/batch")
    public ResponseEntity<List<ProductVariantDto>> getVariantsByIds(@RequestBody List<UUID> ids) {
        List<ProductVariantDto> response = variantService.getVariantsByIds(ids);
        return ResponseEntity.ok(response);
    }
}