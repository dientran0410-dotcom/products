package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends
        JpaRepository<PaymentTransaction, UUID> {
    PaymentTransaction findByOrderId(UUID orderId);

    Optional<PaymentTransaction> findByMomoOrderId(String momoOrderId);
}
