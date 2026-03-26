package com.fsa.franchise.product_service.mapper;

import com.fsa.franchise.product_service.dto.response.ProductCreateResponse;
import org.mapstruct.Mapper;
import com.fsa.franchise.product_service.dto.response.ProductResponse;
import com.fsa.franchise.product_service.dto.response.CategoryResponse;
import com.fsa.franchise.product_service.dto.response.ProductVariantResponse;
import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.entity.Category;
import com.fsa.franchise.product_service.entity.ProductVariant;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {

    ProductCreateResponse toCreateResponse(Product product);

    CategoryResponse toResponse(Category category);

    ProductVariantResponse toResponse(ProductVariant variant);
}
