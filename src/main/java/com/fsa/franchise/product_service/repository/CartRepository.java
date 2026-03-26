package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByCustomerId(UUID customerId);

}
