package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"categories", "reviews", "id", "isDeleted"})
@Getter
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Table(name = "products")
public class Product {
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "categories_products",
      joinColumns = @JoinColumn(name = "product_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "category_id"})
  )
  private final List<Category> categories = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Review> reviews = new ArrayList<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String name;

  @Setter
  private String description;

  @Setter
  @DecimalMin(value = "0.01", message = "Price must be greater than or equal to 0.01")
  @DecimalMax(value = "999999.99", message = "Price must be less than 1'000'000")
  @Column(precision = 8, scale = 2, nullable = false)
  private BigDecimal price;

  @Setter
  @Column(nullable = false)
  private int stockQuantity;

  @Setter
  @Column(precision = 3, scale = 2)
  private BigDecimal averageRating = BigDecimal.ZERO;

  @Setter
  @Column
  private Integer reviewCount = 0;

  @Setter
  @Column(nullable = false)
  private boolean isDeleted = false;

  public void addCategory(Category category) {
    this.categories.add(category);
  }

  public void clearCategories() {
    this.categories.clear();
  }

  public void addReview(Review review) {
    reviews.add(review);
    review.setProduct(this);
    recalculateRating();
  }

  public void removeReview(Review review) {
    reviews.remove(review);
    review.setProduct(null);
    recalculateRating();
  }

  public void recalculateRating() {
    if (this.reviews.isEmpty()) {
      this.averageRating = BigDecimal.ZERO;
      this.reviewCount = 0;
    } else {
      BigDecimal totalRating = this.reviews.stream()
          .map(review -> BigDecimal.valueOf(review.getRating()))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      this.averageRating = totalRating.divide(BigDecimal.valueOf(this.reviews.size()), 2, RoundingMode.HALF_UP);
      this.reviewCount = this.reviews.size();
    }
  }
}
