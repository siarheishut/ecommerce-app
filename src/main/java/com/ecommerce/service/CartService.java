package com.ecommerce.service;

import com.ecommerce.dto.CartViewDto;

public interface CartService {
  void addProductToCart(Long productId, int quantity);

  void removeItem(Long productId);

  void updateProductQuantity(Long productId, int quantity);

  CartViewDto getCartForCurrentUser();
}
