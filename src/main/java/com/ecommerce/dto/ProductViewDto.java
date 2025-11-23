package com.ecommerce.dto;

import com.ecommerce.entity.Product;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductViewDto(Long id, String name, String description, Integer stockQuantity,
                             BigDecimal price, BigDecimal averageRating, Integer reviewCount,
                             int inCartQuantity) implements Serializable {
  public static ProductViewDto fromEntity(Product product, int inCartQuantity) {
    if (product == null) {
      return null;
    }
    return new ProductViewDto(product.getId(), product.getName(), product.getDescription(),
        product.getStockQuantity(), product.getPrice(), product.getAverageRating(),
        product.getReviewCount(), inCartQuantity);
  }

  public int getAvailableForCart() {
    return stockQuantity - inCartQuantity;
  }

  public boolean isAvailableInStock() {
    return getAvailableForCart() > 0;
  }
}
