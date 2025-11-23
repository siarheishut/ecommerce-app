package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.CartItemViewDto;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.dto.ProductViewDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
  private final ProductRepository productRepository;
  private final CartRepository cartRepository;
  private final ShoppingCart sessionCart;
  private final UserService userService;
  private final Lock cartLock;

  @Override
  @Transactional(readOnly = true)
  public CartViewDto getCartForCurrentUser() {
    User user = getCurrentUserOrNull();

    if (user != null) {
      return getDbCartView(user);
    } else {
      return getSessionCartView();
    }
  }

  @Override
  @Transactional
  public void addProductToCart(Long productId, int quantity) {
    User user = getCurrentUserOrNull();

    if (user != null) {
      addProductToDbCart(user, productId, quantity);
    } else {
      cartLock.lock();
      try {
        Product product = getProductOrThrow(productId);
        validateStockForSession(product, quantity);

        ProductViewDto productDto = ProductViewDto.fromEntity(product, quantity);
        sessionCart.addItem(productDto, quantity);
      } finally {
        cartLock.unlock();
      }
    }
  }

  @Override
  @Transactional
  public void updateProductQuantity(Long productId, int quantity) {
    User user = getCurrentUserOrNull();

    if (user != null) {
      updateDbCartQuantity(user, productId, quantity);
    } else {
      cartLock.lock();
      try {
        if (quantity <= 0) {
          sessionCart.removeItem(productId);
          return;
        }
        Product product = getProductOrThrow(productId);
        if (product.getStockQuantity() < quantity) {
          throw new InsufficientStockException("Not enough stock for " + product.getName() +
              ". Available: " + product.getStockQuantity());
        }
        sessionCart.updateItemQuantity(productId, quantity);
      } finally {
        cartLock.unlock();
      }
    }
  }

  @Override
  @Transactional
  public void removeItem(Long productId) {
    User user = getCurrentUserOrNull();
    if (user != null) {
      Cart cart = cartRepository.findByUserWithLock(user).orElse(null);
      if (cart != null) {
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);
      }
    } else {
      cartLock.lock();
      try {
        sessionCart.removeItem(productId);
      } finally {
        cartLock.unlock();
      }
    }
  }

  private void addProductToDbCart(User user, Long productId, int quantity) {
    Product product = getProductOrThrow(productId);
    Cart cart = cartRepository.findByUserWithLock(user)
        .orElseGet(() -> createCartForUser(user));
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getProduct().getId().equals(productId))
        .findFirst();

    int currentQuantity = existingItem.map(CartItem::getQuantity).orElse(0);
    int newQuantity = currentQuantity + quantity;

    if (product.getStockQuantity() < newQuantity) {
      throw new InsufficientStockException("Not enough stock for " + product.getName() +
          ". Available: " + product.getStockQuantity() +
          ". You already have " + currentQuantity + " in cart.");
    }

    if (existingItem.isPresent()) {
      existingItem.get().setQuantity(newQuantity);
    } else {
      CartItem newItem = new CartItem(cart, product, quantity);
      cart.getItems().add(newItem);
    }
    cartRepository.save(cart);
  }

  private void updateDbCartQuantity(User user, Long productId, int quantity) {
    if (quantity <= 0) {
      removeItem(productId);
      return;
    }
    Product product = getProductOrThrow(productId);
    if (product.getStockQuantity() < quantity) {
      throw new InsufficientStockException("Not enough stock for " + product.getName() +
          ". Available: " + product.getStockQuantity());
    }

    Cart cart = cartRepository.findByUserWithLock(user)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    CartItem item = cart.getItems().stream()
        .filter(i -> i.getProduct().getId().equals(productId))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));

    item.setQuantity(quantity);
    cartRepository.save(cart);
  }

  private void validateStockForSession(Product product, int quantity) {
    int currentQuantityInCart = sessionCart.getItems().stream()
        .filter(item -> item.product().id().equals(product.getId()))
        .mapToInt(CartSessionItem::quantity)
        .sum();

    if (product.getStockQuantity() < currentQuantityInCart + quantity) {
      throw new InsufficientStockException("Not enough stock for " + product.getName() +
          ". Available: " + (product.getStockQuantity() - currentQuantityInCart) + ".");
    }
  }

  private CartViewDto getDbCartView(User user) {
    Cart cart = cartRepository.findByUser(user).orElse(new Cart());

    if (cart.getItems().isEmpty()) {
      return new CartViewDto(Collections.emptyList(), BigDecimal.ZERO);
    }

    List<CartItemViewDto> detailedItems = cart.getItems().stream()
        .map(item -> new CartItemViewDto(
            ProductViewDto.fromEntity(item.getProduct(), item.getQuantity())))
        .collect(Collectors.toList());

    BigDecimal totalAmount = detailedItems.stream()
        .map(item -> item.product().price().multiply(new BigDecimal(item.product().inCartQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new CartViewDto(detailedItems, totalAmount);
  }

  private Cart createCartForUser(User user) {
    Cart cart = new Cart();
    cart.setUser(user);
    return cartRepository.save(cart);
  }

  private User getCurrentUserOrNull() {
    try {
      return userService.getCurrentUser();
    } catch (Exception e) {
      return null;
    }
  }

  private CartViewDto getSessionCartView() {
    List<Long> productIds = sessionCart.getItems().stream()
        .map(item -> item.product().id())
        .collect(Collectors.toList());

    if (productIds.isEmpty()) {
      return new CartViewDto(Collections.emptyList(), BigDecimal.ZERO);
    }

    Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<CartItemViewDto> detailedItems = sessionCart.getItems().stream()
        .map(cartItem -> {
          Product product = productMap.get(cartItem.product().id());
          if (product == null) return null;
          return new CartItemViewDto(ProductViewDto.fromEntity(product, cartItem.quantity()));
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    BigDecimal totalAmount = detailedItems.stream()
        .map(item -> item.product().price().multiply(new BigDecimal(item.product().inCartQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new CartViewDto(detailedItems, totalAmount);
  }

  private Product getProductOrThrow(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
  }
}
