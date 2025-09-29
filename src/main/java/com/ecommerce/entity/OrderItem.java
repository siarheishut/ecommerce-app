package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "order_items")
@NoArgsConstructor
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Setter
  @Column(nullable = false)
  private String productName;

  @Setter
  @Column(columnDefinition = "TEXT")
  private String productDescription;

  @Setter
  @Column(nullable = false)
  private int quantity;

  @Setter
  @Column(nullable = false, precision = 8, scale = 2)
  private BigDecimal price;

  public OrderItem(Order order, Product product, int quantity) {
    this.order = order;
    this.product = product;
    this.quantity = quantity;
    this.productName = product.getName();
    this.productDescription = product.getDescription();
    this.price = product.getPrice();
  }
}
