package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
