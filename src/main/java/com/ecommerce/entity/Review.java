package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"user", "product"})
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class Review {
  @Column(nullable = false, updatable = false)
  private final Instant createdAt = Instant.now();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false)
  @Setter
  @NotNull
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @Setter
  @NotNull
  private User user;

  @Column(nullable = false)
  @Min(1)
  @Max(5)
  @Setter
  private Integer rating;

  @Column(columnDefinition = "TEXT")
  @Setter
  @Size(max = 1000, message = "Comment cannot exceed 1000 characters.")
  private String comment;
}
