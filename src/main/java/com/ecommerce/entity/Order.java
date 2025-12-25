package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@NamedEntityGraph(
    name = "Order.withDetailsAndItems",
    attributeNodes = {
        @NamedAttributeNode("shippingDetails"),
        @NamedAttributeNode("orderItems")
    }
)
@Table(name = "orders")
public class Order {
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 20)
  @NotEmpty(message = "Order must contain at least one item.")
  private final List<OrderItem> orderItems = new ArrayList<>();

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false)
  private User user;

  @Setter
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "shipping_details_id", referencedColumnName = "id", nullable = false)
  @NotNull(message = "Shipping details are required.")
  private ShippingDetails shippingDetails;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(nullable = false, updatable = false)
  @NotNull(message = "Order date is required.")
  private Instant orderDate;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "Order status is required.")
  private Status status;

  @Setter
  @NotNull(message = "Total amount is required for an order.")
  @PositiveOrZero(message = "Total amount cannot be negative.")
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  public void addOrderItems(List<OrderItem> items) {
    for (OrderItem item : items) {
      orderItems.add(item);
      item.setOrder(this);
    }
  }

  public enum Status {PENDING, SHIPPED, DELIVERED}
}
