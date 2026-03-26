package com.fsa.franchise.product_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
@Getter
@Setter

public class CategoryUpdateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private UUID parentId;

    private boolean isTopping;

    private String status;

}
