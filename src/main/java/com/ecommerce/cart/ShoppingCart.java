package com.ecommerce.cart;

import com.ecommerce.dto.ProductViewDto;
import com.ecommerce.exception.InsufficientStockException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@SessionScope
public class ShoppingCart implements Serializable {
  private final Map<Long, CartSessionItem> items = new HashMap<>();

  public synchronized void addItem(ProductViewDto product, int quantity) {
    if (product.stockQuantity() < quantity) {
      throw new InsufficientStockException("Not enough stock for product: " + product.name() +
          ".Available: " + product.stockQuantity() + ".");
    }

    CartSessionItem existingItem = items.get(product.id());
    if (existingItem != null) {
      items.put(product.id(), new CartSessionItem(product, existingItem.quantity() + quantity));
    } else {
      items.put(product.id(), new CartSessionItem(product, quantity));
    }
  }

  public synchronized void updateItemQuantity(Long productId, int quantity) {
    CartSessionItem item = items.get(productId);
    if (item != null) {
      if (quantity <= 0) {
        removeItem(productId);
        return;
      }
      ProductViewDto product = item.product();
      items.put(productId, new CartSessionItem(product, quantity));
    }
  }

  public synchronized void removeItem(Long productId) {
    items.remove(productId);
  }

  public synchronized Collection<CartSessionItem> getItems() {
    return Collections.unmodifiableCollection(items.values());
  }

  public synchronized void clear() {
    items.clear();
  }

  public synchronized BigDecimal getTotalAmount() {
    return items.values().stream()
        .map(item -> item.product().price().multiply(new BigDecimal(item.quantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
