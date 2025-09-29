package com.ecommerce.dto;

import com.ecommerce.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record OrderHistoryDto(
    Long orderId,
    Instant orderDate,
    Order.Status status,
    BigDecimal totalAmount,
    List<OrderHistoryItemDto> items
) {
  public static OrderHistoryDto fromEntity(Order order) {
    if (order == null) {
      return null;
    }
    List<OrderHistoryItemDto> itemDtos = order.getOrderItems().stream()
        .map(item -> new OrderHistoryItemDto(
            item.getProductName(),
            item.getProductDescription(),
            item.getQuantity(),
            item.getPrice()
        ))
        .collect(Collectors.toList());

    return new OrderHistoryDto(
        order.getId(),
        order.getOrderDate(),
        order.getStatus(),
        order.getTotalAmount(),
        itemDtos
    );
  }
}
