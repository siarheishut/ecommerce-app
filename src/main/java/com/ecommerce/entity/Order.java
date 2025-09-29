package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "orders")
public class Order {
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @NotEmpty
  private final List<OrderItem> orderItems = new ArrayList<>();
  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false)
  private User user;
  @Setter
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "shipping_details_id", referencedColumnName = "id", nullable = false)
  @NotNull
  private ShippingDetails shippingDetails;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(nullable = false, updatable = false)
  @NotNull
  private Instant orderDate;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull
  private Status status;

  @Setter
  @Column(nullable = false, precision = 38, scale = 2)
  @NotNull @PositiveOrZero
  private BigDecimal totalAmount;

  public void addOrderItems(List<OrderItem> items) {
    for (OrderItem item : items) {
      orderItems.add(item);
      item.setOrder(this);
    }
  }

  public enum Status {PENDING, SHIPPED, DELIVERED}
}
