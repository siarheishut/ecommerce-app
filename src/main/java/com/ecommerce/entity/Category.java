package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "categories")
@SQLDelete(sql = "UPDATE categories SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@EqualsAndHashCode(of = "name")
public class Category {
  @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
  private final List<Product> products = new ArrayList<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(nullable = false, unique = true)
  @NotBlank(message = "Category name is required.")
  private String name;

  @Setter
  @Column(nullable = false)
  private boolean isDeleted = false;

  public Category(String name) {
    this.name = name;
  }

  public String getBaseName() {
    if (this.name != null && this.name.contains("_deleted_")) {
      return this.name.substring(0, this.name.lastIndexOf("_deleted_"));
    }
    return this.name;
  }
}
