package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
  @NotNull(message = "Order item must be associated with an order.")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Setter
  @NotNull(message = "Order item must be associated with a product.")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Setter
  @NotNull(message = "Product name is required for an order item.")
  @Column(nullable = false)
  private String productName;

  @Setter
  @Column(columnDefinition = "TEXT")
  private String productDescription;

  @Setter
  @Positive(message = "Quantity must be at least 1.")
  @Column(nullable = false)
  private int quantity;

  @Setter
  @NotNull(message = "Price is required for an order item.")
  @DecimalMin(value = "0.01", message = "Price must be at least 0.01.")
  @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000.")
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
