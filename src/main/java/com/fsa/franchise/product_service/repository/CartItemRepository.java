package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndVariantId(Long cartId, UUID variantId);

    List<CartItem> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);
}
