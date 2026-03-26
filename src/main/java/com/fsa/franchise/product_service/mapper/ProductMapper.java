package com.fsa.franchise.product_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import com.fsa.franchise.product_service.dto.request.ProductUpdateRequest;
import com.fsa.franchise.product_service.dto.response.ProductResponse;
import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.entity.ProductVariant;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "price", expression = "java(getDefaultPrice(product))")
    @Mapping(target = "sku", expression = "java(getDefaultSku(product))")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);

    // Hàm hỗ trợ MapStruct lấy Price từ Variant mặc định
    default BigDecimal getDefaultPrice(Product product) {
        if (product.getVariants() == null) return null;
        return product.getVariants().stream()
                .filter(ProductVariant::isDefault)
                .findFirst()
                .map(ProductVariant::getPrice)
                .orElse(null);
    }

    // Hàm hỗ trợ MapStruct lấy SKU từ Variant mặc định
    default String getDefaultSku(Product product) {
        if (product.getVariants() == null) return null;
        return product.getVariants().stream()
                .filter(ProductVariant::isDefault)
                .findFirst()
                .map(ProductVariant::getSku)
                .orElse(null);
    }
}
