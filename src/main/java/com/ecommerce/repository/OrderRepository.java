package com.ecommerce.repository;

import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.OrderHistoryItemDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @Query("""
      SELECT new com.ecommerce.dto.OrderHistoryDto(o.id, o.orderDate, o.status, o.totalAmount)
      FROM Order o
      WHERE o.user = :user
      ORDER BY o.orderDate DESC
      """)
  List<OrderHistoryDto> findOrderHistoryByUser(@Param("user") User user);

  @Query("""
      SELECT new com.ecommerce.dto.OrderHistoryItemDto(oi.order.id, oi.productName, oi.productDescription, oi.quantity, oi.price)
      FROM OrderItem oi
      WHERE oi.order.id IN :orderIds
      """)
  List<OrderHistoryItemDto> findOrderHistoryItemsByOrderIds(@Param("orderIds") List<Long> orderIds);
}
