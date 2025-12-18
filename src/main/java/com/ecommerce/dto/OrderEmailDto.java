package com.ecommerce.dto;

import com.ecommerce.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEmailDto(
    Long orderId,
    String email,
    String firstName,
    Instant orderDate,
    BigDecimal totalPrice
) {

  static public OrderEmailDto fromEntity(Order order) {
    return new OrderEmailDto(
        order.getId(), order.getShippingDetails().getEmail(),
        order.getShippingDetails().getFirstName(), order.getOrderDate(), order.getTotalAmount()
    );
  }
}
