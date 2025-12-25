package com.ecommerce.repository;

import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.OrderHistoryItemDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Override
  @EntityGraph(value = "Order.withDetailsAndItems")
  @NonNull
  Optional<Order> findById(@NonNull Long id);

  @Query("""
      SELECT new com.ecommerce.dto.OrderHistoryDto(o.id, o.orderDate, o.status, o.totalAmount)
      FROM Order o
      WHERE o.user = :user
      ORDER BY o.orderDate DESC
      """)
  List<OrderHistoryDto> findOrderHistoryByUser(@Param("user") User user);

  @Query("""
      SELECT new com.ecommerce.dto.OrderHistoryItemDto(oi.product.id, oi.order.id, oi.productName, oi.productDescription, oi.quantity, oi.price)
      FROM OrderItem oi
      WHERE oi.order.id IN :orderIds
      """)
  List<OrderHistoryItemDto> findOrderHistoryItemsByOrderIds(@Param("orderIds") List<Long> orderIds);
}
