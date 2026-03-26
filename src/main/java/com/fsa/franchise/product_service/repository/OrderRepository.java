package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import java.util.List;


import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    @Override
    @EntityGraph(attributePaths = {"items", "items.addons"})
    List<Order> findAll(Specification<Order> spec, Sort sort);

    @Query("SELECT o FROM Order o WHERE " +
           "(cast(:franchiseId as uuid) IS NULL OR o.franchiseId = :franchiseId) AND " +
           "(cast(:startDate as timestamp) IS NULL OR o.createdAt >= :startDate) AND " +
           "(cast(:endDate as timestamp) IS NULL OR o.createdAt <= :endDate)")
    List<Order> findOrdersForReport(
            @org.springframework.data.repository.query.Param("franchiseId") UUID franchiseId, 
            @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate, 
            @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);

    List<Order> findByCustomerId(UUID customerId);
}