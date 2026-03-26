package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.request.UpdateCartItemRequest;
import com.fsa.franchise.product_service.entity.CartItem;
import com.fsa.franchise.product_service.repository.CartItemRepository;
import com.fsa.franchise.product_service.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public void updateCartItem(UpdateCartItemRequest request) {

        CartItem cartItem = cartItemRepository
                .findById(request.getCartItemId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
            return;
        }

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteCartItem(Long cartItemId) {

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }
}
