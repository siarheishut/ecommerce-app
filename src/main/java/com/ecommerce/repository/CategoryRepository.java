package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findAllByOrderByNameAsc();

  Optional<Category> findByNameIgnoreCase(String name);

  @Query(value = "SELECT * FROM categories ORDER BY name ASC", nativeQuery = true)
  List<Category> findAllWithDeleted();

  @Query(value = "SELECT * FROM categories WHERE id = :id", nativeQuery = true)
  Optional<Category> findByIdWithDeleted(Long id);
}
