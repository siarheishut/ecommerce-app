package com.ecommerce.dto;

import java.math.BigDecimal;

public record OrderHistoryItemDto(
    String productName,
    String productDescription,
    int quantity,
    BigDecimal price
) {
}
