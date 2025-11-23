package com.ecommerce.dto;

import java.math.BigDecimal;

public record OrderHistoryItemDto(
    Long productId,
    Long orderId,
    String productName,
    String productDescription,
    int quantity,
    BigDecimal price
) {
}
