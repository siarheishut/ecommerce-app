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

  // Constructor for JPQL projections.
  public OrderHistoryDto(Long orderId, Instant orderDate, Order.Status status,
                         BigDecimal totalAmount) {
    this(orderId, orderDate, status, totalAmount, null);
  }

  /**
   * It is kept for potential future use cases where a
   * fully loaded entity is available and needs conversion.
   */
  @Deprecated(forRemoval = false)
  public static OrderHistoryDto fromEntity(Order order) {
    if (order == null) {
      return null;
    }
    List<OrderHistoryItemDto> itemDtos = order.getOrderItems().stream()
        .map(item -> new OrderHistoryItemDto(
            item.getProduct().getId(),
            order.getId(),
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
