package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.request.CartRequest;
import com.fsa.franchise.product_service.dto.request.UpdateCartItemRequest;
import com.fsa.franchise.product_service.service.CartItemService;
import com.fsa.franchise.product_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products/cart")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequest cartRequest) {
        cartService.addToCart(cartRequest);
        return ResponseEntity.ok(java.util.Map.of("message", "Product added to cart successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestParam UUID customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PutMapping("/item")
    public ResponseEntity<?> updateCartItem(@RequestBody UpdateCartItemRequest request) {

        cartItemService.updateCartItem(request);

        return ResponseEntity.ok("Cart updated");
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long id) {

        cartItemService.deleteCartItem(id);

        return ResponseEntity.ok("Item deleted");
    }

}
