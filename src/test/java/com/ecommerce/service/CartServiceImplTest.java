package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.CartItemViewDto;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {
  @InjectMocks
  CartServiceImpl cartService;
  @Mock
  private ProductRepository productRepository;
  @Mock
  private ShoppingCart shoppingCart;
  @Spy
  private Lock cartLock = new ReentrantLock();

  @Test
  public void whenAddProductToCart_withNotFoundId_throwResourceNotFoundException() {
    Long productId = 1L;
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> cartService.addProductToCart(productId, 2)
    );

    verify(shoppingCart, never()).addItem(any(Product.class), eq(2));
    verify(cartLock).lock();
    verify(cartLock).unlock();
    assertThat(exception.getMessage()).isEqualTo("Product with ID 1 not found.");
  }

  @Test
  void whenAddProductToCart_withSufficientStock_addsItem() {
    Long productId = 1L;
    int quantity = 2;
    Product product = new Product();
    product.setStockQuantity(10);

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(shoppingCart.getItems()).thenReturn(Collections.emptyList());

    cartService.addProductToCart(productId, quantity);

    verify(shoppingCart).addItem(product, quantity);
    verify(cartLock).lock();
    verify(cartLock).unlock();
  }

  @Test
  void whenAddProductToCart_withInsufficientStock_throwsInsufficientStockException() {
    Long productId = 1L;
    int quantity = 5;
    Product product = new Product();
    product.setName("Test Product");
    product.setStockQuantity(4);

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(shoppingCart.getItems()).thenReturn(Collections.emptyList());

    InsufficientStockException exception = assertThrows(
        InsufficientStockException.class,
        () -> cartService.addProductToCart(productId, quantity)
    );

    assertThat(exception.getMessage())
        .isEqualTo("Not enough stock for Test Product. Available: 4.");
    verify(shoppingCart, never()).addItem(any(), anyInt());
    verify(cartLock).lock();
    verify(cartLock).unlock();
  }

  @Test
  void whenUpdateProductQuantity_withSufficientStock_updatesItem() {
    Long productId = 1L;
    int quantity = 3;
    Product product = new Product();
    product.setStockQuantity(5);

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    cartService.updateProductQuantity(productId, quantity);

    verify(shoppingCart).updateItemQuantity(productId, quantity);
    verify(cartLock).lock();
    verify(cartLock).unlock();
  }

  @Test
  void whenUpdateProductQuantity_withInsufficientStock_throwsInsufficientStockException() {
    Long productId = 1L;
    int quantity = 10;
    Product product = new Product();
    product.setName("Test Product");
    product.setStockQuantity(5);

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    InsufficientStockException exception = assertThrows(
        InsufficientStockException.class,
        () -> cartService.updateProductQuantity(productId, quantity)
    );

    assertThat(exception.getMessage()).isEqualTo("Not enough stock for Test Product. Available: 5");
    verify(shoppingCart, never()).updateItemQuantity(anyLong(), anyInt());
    verify(cartLock).lock();
    verify(cartLock).unlock();
  }

  @Test
  void whenRemoveItem_delegatesToShoppingCart() {
    Long productId = 1L;

    cartService.removeItem(productId);

    verify(shoppingCart).removeItem(productId);
    verify(cartLock).lock();
    verify(cartLock).unlock();
  }

  @Test
  void whenGetCartForCurrentUser_withEmptyCart_returnsEmptyCartView() {
    when(shoppingCart.getItems()).thenReturn(Collections.emptyList());

    CartViewDto cartView = cartService.getCartForCurrentUser();

    assertThat(cartView.items()).isEmpty();
    assertThat(cartView.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    verify(productRepository, never()).findAllById(any());
  }

  @Test
  void whenGetCartForCurrentUser_withItems_returnsPopulatedCartView() {
    Product product1 = mock(Product.class);
    when(product1.getId()).thenReturn(1L);
    when(product1.getName()).thenReturn("Chandelier");
    when(product1.getPrice()).thenReturn(new BigDecimal("1200.00"));

    Product product2 = mock(Product.class);
    when(product2.getId()).thenReturn(2L);
    when(product2.getName()).thenReturn("Box");
    when(product2.getPrice()).thenReturn(new BigDecimal("25.50"));

    CartSessionItem item1 = new CartSessionItem(product1, 1);
    CartSessionItem item2 = new CartSessionItem(product2, 2);

    when(shoppingCart.getItems()).thenReturn(List.of(item1, item2));
    when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(product1, product2));

    CartViewDto cartView = cartService.getCartForCurrentUser();
    List<CartItemViewDto> items = cartView.items().stream().toList();

    assertThat(items).hasSize(2);
    assertThat(items.get(0).product().name()).isEqualTo("Chandelier");
    assertThat(items.get(0).quantity()).isEqualTo(1);
    assertThat(items.get(1).product().name()).isEqualTo("Box");
    assertThat(items.get(1).quantity()).isEqualTo(2);

    assertThat(cartView.totalAmount()).isEqualByComparingTo("1251.00");
  }
}
