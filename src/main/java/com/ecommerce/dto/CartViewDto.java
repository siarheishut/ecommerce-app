package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.Collection;

public record CartViewDto(
    Collection<CartItemViewDto> items,
    BigDecimal totalAmount) {
}
