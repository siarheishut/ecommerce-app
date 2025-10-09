package com.ecommerce.repository;

import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Query(value = "SELECT p.id, p.name, p.description, p.price, p.stock_quantity AS stockQuantity, " +
      "GROUP_CONCAT(c.name ORDER BY c.name SEPARATOR ', ') AS categoriesString, " +
      "p.is_deleted AS isDeleted " +
      "FROM products p LEFT JOIN categories_products cp ON p.id = cp.product_id " +
      "LEFT JOIN categories c ON cp.category_id = c.id " +
      "GROUP BY p.id, p.name, p.description, p.price, p.stock_quantity, p.is_deleted " +
      "ORDER BY p.name ASC",
      nativeQuery = true)
  List<ProductAdminView> findAllForAdminView();

  @Modifying
  @Query(value = "UPDATE products SET is_deleted = false WHERE id = :id", nativeQuery = true)
  void restoreById(Long id);

  @Query(value = "SELECT * FROM products WHERE id = :id", nativeQuery = true)
  Optional<Product> findByIdWithDeleted(Long id);
}
