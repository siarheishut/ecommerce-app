package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderDto(@NotEmpty(message = "Order must contain at least one item.") @Valid List<OrderItemDto> orderItemsList) {
}
