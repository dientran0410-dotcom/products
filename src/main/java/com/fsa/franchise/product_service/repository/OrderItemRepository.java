package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    @Query("""
SELECT oi
FROM OrderItem oi
JOIN oi.order o
WHERE (cast(:franchiseId as uuid) IS NULL OR o.franchiseId = :franchiseId)
AND (cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate)
AND (cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)
""")
    List<OrderItem> findOrderItemsForReport(
            @Param("franchiseId") UUID franchiseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
