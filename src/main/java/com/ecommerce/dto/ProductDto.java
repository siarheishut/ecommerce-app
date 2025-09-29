package com.ecommerce.dto;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProductDto {
  private Long id;

  @NotBlank(message = "Product name is required")
  private String name;

  private String description;

  @NotNull(message = "Price is required")
  @Digits(integer = 8, fraction = 2)
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  @DecimalMax(value = "999999.99", message = "Price must be less than 1'000'000")
  private BigDecimal price;

  @NotNull(message = "Stock quantity is required")
  @Min(value = 0, message = "Stock quantity cannot be negative")
  @Max(value = 999999, message = "Stock quantity must be less than 1'000'000")
  private int stockQuantity;

  private List<Long> categories = new ArrayList<>();

  public static ProductDto fromEntity(Product product) {
    ProductDto dto = new ProductDto();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setDescription(product.getDescription());
    dto.setPrice(product.getPrice());
    dto.setStockQuantity(product.getStockQuantity());
    dto.setCategories(product.getCategories().stream()
        .map(Category::getId)
        .collect(Collectors.toList()));
    return dto;
  }
}
