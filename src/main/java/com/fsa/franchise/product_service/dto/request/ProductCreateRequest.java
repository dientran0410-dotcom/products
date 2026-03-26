package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProductCreateRequest {
    @NotBlank(message = "Product name cannot be blank")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private String imageUrl;

    @NotEmpty(message = "At least one variant is required")
    @Valid
    private List<VariantRequest> variants;

    @Getter
    @Setter
    public static class VariantRequest {
        @NotBlank(message = "Variant name cannot be blank")
        private String name;

        @NotNull(message = "Price is required")
        private BigDecimal price;

        private boolean isDefault = false;

        @NotEmpty(message = "At least one ingredient is required for the recipe")
        @Valid
        private List<IngredientRequest> ingredients;
    }

    @Getter
    @Setter
    public static class IngredientRequest {
        @NotNull(message = "Ingredient ID is required")
        private UUID ingredientId;

        @NotNull(message = "Quantity is required")
        private BigDecimal quantity;
    }
}