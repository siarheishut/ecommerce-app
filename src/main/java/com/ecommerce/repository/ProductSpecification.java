package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProductSpecification {
  public static Specification<Product> hasName(String name) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
  }

  public static Specification<Product> inCategories(List<Long> categoryIds) {
    return (root, query, criteriaBuilder) -> {
      query.distinct(true);
      return root.join("categories").get("id").in(categoryIds);
    };
  }

  public static Specification<Product> inPriceRange(Double minPrice, Double maxPrice) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
  }

  public static Specification<Product> isAvailable() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThan(root.get("stockQuantity"), 0);
  }
}
