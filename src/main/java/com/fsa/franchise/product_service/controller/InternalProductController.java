package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.response.ExternalProductResponse;
import com.fsa.franchise.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products/public/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ExternalProductResponse>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }
}
