package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.request.UpdateCartItemRequest;

public interface CartItemService {

    void updateCartItem(UpdateCartItemRequest request);

    void deleteCartItem(Long cartItemId);

}
