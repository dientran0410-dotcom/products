package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.OrderAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderAuditLogRepository extends JpaRepository<OrderAuditLog, UUID> {
}
