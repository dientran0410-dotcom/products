package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductUpdateRequest {

    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private String imageUrl;

    private String status;
}
