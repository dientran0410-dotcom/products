package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryCreateRequest {

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private UUID parentId;

    private Boolean isTopping;
}