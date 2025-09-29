package com.ecommerce.dto;

import com.ecommerce.entity.Product;

import java.math.BigDecimal;

public record ProductViewDto(Long id, String name, String description, Integer stockQuantity,
                             BigDecimal price, BigDecimal averageRating, Integer reviewCount) {
  public static ProductViewDto fromEntity(Product product) {
    if (product == null) {
      return null;
    }
    return new ProductViewDto(product.getId(), product.getName(), product.getDescription(),
        product.getStockQuantity(), product.getPrice(), product.getAverageRating(),
        product.getReviewCount());
  }
}
