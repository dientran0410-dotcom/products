package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.request.CartRequest;
import com.fsa.franchise.product_service.dto.response.CartResponse;
import com.fsa.franchise.product_service.entity.Cart;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CartService {

        @Transactional
        void addToCart(CartRequest cartRequest);

        Cart getCartByCustomerId(UUID customerId);

        CartResponse getCart(UUID customerId);

}
