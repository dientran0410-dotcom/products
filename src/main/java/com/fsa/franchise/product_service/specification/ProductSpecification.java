package com.fsa.franchise.product_service.specification;

import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.entity.ProductVariant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> filterProducts(String keyword, UUID categoryId, String status) {
        return (root, query, cb) -> {
            // Sử dụng Specification.where(null) thay vì cast null
            Specification<Product> spec = Specification.where((r, q, builder) -> builder.conjunction());

            // 1. Tìm kiếm theo Name của Product HOẶC SKU của Variant (Chỉ join khi cần thiết)
            if (StringUtils.hasText(keyword)) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                spec = spec.and((r, q, builder) -> {
                    // Đảm bảo không bị lặp record khi join
                    q.distinct(true);

                    // Join sang bảng variants để lấy trường sku
                    Join<Product, ProductVariant> variantJoin = r.join("variants", JoinType.LEFT);

                    return builder.or(
                            builder.like(builder.lower(r.get("name")), searchPattern),
                            builder.like(builder.lower(variantJoin.get("sku")), searchPattern)
                    );
                });
            }

            // 2. Lọc theo Category ID
            if (categoryId != null) {
                spec = spec.and((r, q, builder) ->
                        builder.equal(r.get("category").get("id"), categoryId));
            }

            // 3. Lọc theo Status (ACTIVE, INACTIVE, v.v...)
            if (StringUtils.hasText(status)) {
                spec = spec.and((r, q, builder) ->
                        builder.equal(r.get("status"), status));
            }

            // Luôn đảm bảo không lấy các record đã xóa (Soft Delete)
            // Nếu @SQLRestriction chưa bao phủ hết các trường hợp join
            // spec = spec.and((r, q, builder) -> builder.isNull(r.get("deletedAt")));

            return spec.toPredicate(root, query, cb);
        };
    }
}