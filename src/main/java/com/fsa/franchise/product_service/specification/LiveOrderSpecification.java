package com.fsa.franchise.product_service.specification;

import com.fsa.franchise.product_service.entity.Order;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;

public class LiveOrderSpecification {

    public static Specification<Order> getActiveOrders() {
        return (root, query, criteriaBuilder) -> {
            // Fetch items and addons to avoid LazyInitializationException
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("items", JoinType.LEFT).fetch("addons", JoinType.LEFT);
                query.distinct(true);
            }
            
            return criteriaBuilder.and(
                criteriaBuilder.notEqual(root.get("status"), Order.OrderStatus.COMPLETED),
                criteriaBuilder.notEqual(root.get("status"), Order.OrderStatus.CANCELLED)
            );
        };
    }
}
