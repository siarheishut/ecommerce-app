package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findAllByOrderByNameAsc();

  Optional<Category> findByNameIgnoreCase(String name);

  @Query(value = """
      SELECT * FROM categories c
      WHERE (:status = 'all' OR c.is_deleted = :is_deleted)
      ORDER BY c.name ASC
      """, nativeQuery = true)
  List<Category> findAllWithDeleted(String status, boolean is_deleted);

  @Query(value = "SELECT * FROM categories WHERE id = :id", nativeQuery = true)
  Optional<Category> findByIdWithDeleted(Long id);

  @Query(value = """
      SELECT * FROM categories c
      WHERE lower(c.name) LIKE lower(concat('%', :keyword, '%'))
      AND (:status = 'all' OR c.is_deleted = :is_deleted)
      ORDER BY c.name ASC
      """, nativeQuery = true)
  List<Category> searchByNameForAdmin(String keyword, String status, boolean is_deleted);
}
