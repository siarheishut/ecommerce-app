package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.ProductViewDto;
import com.ecommerce.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShoppingCartTest {

  private ShoppingCart shoppingCart;

  @BeforeEach
  void setUp() {
    shoppingCart = new ShoppingCart();
  }

  private ProductViewDto createProduct(Long id, String name, String price, int stock) {
    return new ProductViewDto(
        id, name, "description", stock, new BigDecimal(price), BigDecimal.ZERO, 0, 0);
  }

  @Test
  void whenAddItem_withSufficientStock_addsItemToCart() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);

    assertThat(shoppingCart.getItems()).hasSize(1);
    CartSessionItem item = shoppingCart.getItems().iterator().next();
    assertThat(item.product()).isEqualTo(product);
    assertThat(item.quantity()).isEqualTo(1);
  }

  @Test
  void whenAddItem_withExistingItem_updatesQuantity() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);
    shoppingCart.addItem(product, 2);

    assertThat(shoppingCart.getItems()).hasSize(1);
    CartSessionItem item = shoppingCart.getItems().iterator().next();
    assertThat(item.quantity()).isEqualTo(3);
  }

  @Test
  void whenAddItem_withInsufficientStock_throwsInsufficientStockException() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 5);

    InsufficientStockException exception = assertThrows(
        InsufficientStockException.class,
        () -> shoppingCart.addItem(product, 6)
    );

    assertThat(exception.getMessage()).isEqualTo("Not enough stock for product: Laptop.Available: 5.");
    assertThat(shoppingCart.getItems()).isEmpty();
  }

  @Test
  void whenUpdateItemQuantity_withPositiveQuantity_updatesQuantity() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);
    shoppingCart.updateItemQuantity(1L, 5);

    assertThat(shoppingCart.getItems()).hasSize(1);
    CartSessionItem item = shoppingCart.getItems().iterator().next();
    assertThat(item.quantity()).isEqualTo(5);
  }

  @Test
  void whenUpdateItemQuantity_withZeroQuantity_removesItem() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);
    shoppingCart.updateItemQuantity(1L, 0);

    assertThat(shoppingCart.getItems()).isEmpty();
  }

  @Test
  void whenUpdateItemQuantity_withNegativeQuantity_removesItem() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);
    shoppingCart.updateItemQuantity(1L, -1);

    assertThat(shoppingCart.getItems()).isEmpty();
  }

  @Test
  void whenUpdateItemQuantity_forNonExistentItem_doesNothing() {
    shoppingCart.updateItemQuantity(99L, 5);
    assertThat(shoppingCart.getItems()).isEmpty();
  }

  @Test
  void whenRemoveItem_withExistingItem_removesItFromCart() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);
    assertThat(shoppingCart.getItems()).hasSize(1);

    shoppingCart.removeItem(1L);
    assertThat(shoppingCart.getItems()).isEmpty();
  }

  @Test
  void whenRemoveItem_withNonExistentItem_doesNothing() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);
    assertThat(shoppingCart.getItems()).hasSize(1);

    shoppingCart.removeItem(99L);
    assertThat(shoppingCart.getItems()).hasSize(1);
  }

  @Test
  void whenGetItems_returnsUnmodifiableCollection() {
    ProductViewDto product = createProduct(1L, "Laptop", "1500.00", 10);
    shoppingCart.addItem(product, 1);

    assertThrows(UnsupportedOperationException.class, () ->
        shoppingCart.getItems().add(new CartSessionItem(product, 2))
    );
  }

  @Test
  void whenClear_removesAllItems() {
    shoppingCart.addItem(createProduct(1L, "Laptop", "1500.00", 10), 1);
    shoppingCart.addItem(createProduct(2L, "Mouse", "75.00", 20), 2);
    assertThat(shoppingCart.getItems()).hasSize(2);

    shoppingCart.clear();
    assertThat(shoppingCart.getItems()).isEmpty();
  }

  @Test
  void whenGetTotalAmount_withEmptyCart_returnsZero() {
    assertThat(shoppingCart.getTotalAmount()).isEqualByComparingTo("0");
  }

  @Test
  void whenGetTotalAmount_withItems_returnsCorrectTotal() {
    shoppingCart.addItem(createProduct(1L, "Laptop", "1500.50", 10), 1);
    shoppingCart.addItem(createProduct(2L, "Mouse", "75.25", 20), 2);

    BigDecimal expectedTotal = new BigDecimal("1651.00");
    assertThat(shoppingCart.getTotalAmount()).isEqualByComparingTo(expectedTotal);
  }
}
