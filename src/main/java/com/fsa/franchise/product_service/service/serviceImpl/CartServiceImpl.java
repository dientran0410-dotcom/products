package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.request.CartRequest;
import com.fsa.franchise.product_service.dto.response.CartItemResponse;
import com.fsa.franchise.product_service.dto.response.CartResponse;
import com.fsa.franchise.product_service.entity.Cart;
import com.fsa.franchise.product_service.entity.CartItem;
import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.entity.ProductVariant;
import com.fsa.franchise.product_service.repository.CartItemRepository;
import com.fsa.franchise.product_service.repository.CartRepository;
import com.fsa.franchise.product_service.repository.ProductRepository;
import com.fsa.franchise.product_service.repository.ProductVariantRepository;
import com.fsa.franchise.product_service.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository  productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public void addToCart(CartRequest cartRequest) {

        if(cartRequest.getQuantity() <= 0 || cartRequest.getQuantity() == null) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository
                .findByCustomerId(cartRequest.getCustomerId());

        if (cart == null) {
            cart = new Cart();
            // ensure the cart is tied to the requesting customer
            cart.setCustomerId(cartRequest.getCustomerId());
            cartRepository.save(cart);
        }

        ProductVariant variant = productVariantRepository.findById(cartRequest.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (cartRequest.getProductId() != null
            && !variant.getProduct().getId().equals(cartRequest.getProductId())) {
            throw new RuntimeException("Variant does not belong to the provided productId");
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndVariantId(cart.getId(), variant.getId());

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + cartRequest.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCartId(cart.getId());
            cartItem.setProductId(variant.getProduct().getId());
            cartItem.setVariantId(variant.getId());
            cartItem.setQuantity(cartRequest.getQuantity());

            cartItem.setPrice(variant.getPrice());
        }

        cartItemRepository.save(cartItem);
    }

    public CartResponse getCart(UUID customerId) {

        Cart cart = cartRepository.findByCustomerId(customerId);

        if (cart == null) {
            return new CartResponse();
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> responses = cartItems.stream().map(cartItem -> {

            ProductVariant variant = productVariantRepository
                    .findById(cartItem.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            Product product = variant.getProduct();

            CartItemResponse cartItemResponse = new CartItemResponse();

            cartItemResponse.setCartItemId(cartItem.getId());
            cartItemResponse.setProductId(cartItem.getProductId());
            cartItemResponse.setProductName(product.getName());
            cartItemResponse.setProductImage(product.getImageUrl());
            cartItemResponse.setQuantity(cartItem.getQuantity());

            cartItemResponse.setProductPrice(cartItem.getPrice());

            BigDecimal totalPrice = cartItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            cartItemResponse.setTotalPrice(totalPrice);

            return cartItemResponse;
        }).toList();

        BigDecimal subtotal = responses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartId(cart.getId());
        cartResponse.setItems(responses);
        cartResponse.setSubtotal(subtotal);

        return cartResponse;
    }

    public Cart getCartByCustomerId(UUID customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId);
        if (cart == null) {
            throw new RuntimeException("Cart not found for customerId: " + customerId);
        }
        return cart;
    }

}
