package com.ecommerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemDto {
  @NotNull(message = "Product ID is required.")
  private Long productId;
  @Min(value = 1, message = "Quantity must be at least 1")
  @Max(value = 1000, message = "Quantity cannot exceed 1,000.")
  private int quantity;
}
