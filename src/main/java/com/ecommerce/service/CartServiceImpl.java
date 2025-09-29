package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.CartItemViewDto;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.dto.ProductViewDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
  private final ProductRepository productRepository;
  private final ShoppingCart shoppingCart;
  private final Lock cartLock = new ReentrantLock();

  @Override
  @Transactional
  public void addProductToCart(Long productId, int quantity) {
    cartLock.lock();
    try {
      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new ResourceNotFoundException(
              "Product with ID " + productId + " not found"));

      int currentQuantityInCart = shoppingCart.getItems().stream()
          .filter(item -> item.product().getId().equals(productId))
          .mapToInt(CartSessionItem::quantity)
          .sum();

      if (product.getStockQuantity() < currentQuantityInCart + quantity) {
        throw new InsufficientStockException("Not enough stock for " + product.getName() +
            ". Available: " + product.getStockQuantity());
      }
      shoppingCart.addItem(product, quantity);
    } finally {
      cartLock.unlock();
    }
  }

  @Override
  @Transactional
  public void updateProductQuantity(Long productId, int quantity) {
    cartLock.lock();
    try {
      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new ResourceNotFoundException(
              "Product with ID " + productId + " not found"));

      if (product.getStockQuantity() < quantity) {
        throw new InsufficientStockException("Not enough stock for " + product.getName() +
            ". Available: " + product.getStockQuantity());
      }
      shoppingCart.updateItemQuantity(productId, quantity);
    } finally {
      cartLock.unlock();
    }
  }

  @Override
  @Transactional
  public void removeItem(Long productId) {
    cartLock.lock();
    try {
      shoppingCart.removeItem(productId);
    } finally {
      cartLock.unlock();
    }
  }

  @Override
  @Transactional
  public CartViewDto getCartForCurrentUser() {
    List<Long> productIds = shoppingCart.getItems().stream()
        .map(item -> item.product().getId())
        .collect(Collectors.toList());

    if (productIds.isEmpty()) {
      return new CartViewDto(Collections.emptyList(), BigDecimal.ZERO);
    }

    Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<CartItemViewDto> detailedItems = shoppingCart.getItems().stream()
        .map(cartItem -> {
          Product product = productMap.get(cartItem.product().getId());
          if (product == null) {
            return null;
          }
          return new CartItemViewDto(ProductViewDto.fromEntity(product), cartItem.quantity());
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    BigDecimal totalAmount = detailedItems.stream()
        .map(item -> item.product().price().multiply(new BigDecimal(item.quantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new CartViewDto(detailedItems, totalAmount);
  }
}
