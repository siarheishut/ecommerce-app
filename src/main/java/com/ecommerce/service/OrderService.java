package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.ShippingDetailsDto;
import com.ecommerce.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
  Optional<Order> findById(Long id);

  Order placeOrder(String username, OrderDto orderDto);

  void placeOrder(ShippingDetailsDto shippingDetailsDto);

  List<OrderHistoryDto> getOrderHistoryForCurrentUser();
}
