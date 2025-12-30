package com.ecommerce.repository;

import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Query(value = """
      SELECT p.id, p.name, p.description, p.price, p.stock_quantity AS stockQuantity,
        GROUP_CONCAT(c.name ORDER BY c.name SEPARATOR ', ') AS categoriesString, 
        p.is_deleted AS isDeleted
      FROM products p 
      LEFT JOIN categories_products cp ON p.id = cp.product_id
      LEFT JOIN categories c ON cp.category_id = c.id
      GROUP BY p.id, p.name, p.description, p.price, p.stock_quantity, p.is_deleted
      ORDER BY p.name ASC
      """, nativeQuery = true)
  List<ProductAdminView> findAllForAdminView();

  @Query(value = """
      SELECT p.id, p.name, p.description, p.price, p.stock_quantity AS stockQuantity,
        (SELECT GROUP_CONCAT(c2.name ORDER BY c2.name SEPARATOR ', ')
            FROM categories c2
            JOIN categories_products cp2 ON c2.id = cp2.category_id
            WHERE cp2.product_id = p.id) AS categoriesString,  
        p.is_deleted AS isDeleted
      FROM products p 
      LEFT JOIN categories_products cp ON p.id = cp.product_id
      LEFT JOIN categories c ON cp.category_id = c.id
      WHERE (COALESCE(:categoryIds) IS NULL OR c.id IN (:categoryIds))
      AND (p.name LIKE CONCAT('%', :keyword, '%'))
      AND (:status = 'all' OR p.is_deleted = :isDeleted)
      GROUP BY p.id, p.name, p.description, p.price, p.stock_quantity, p.is_deleted
      HAVING (COALESCE(:categoryIds) IS NULL OR COUNT(DISTINCT c.id) >= :categoryCount)
      ORDER BY p.name ASC
      """, nativeQuery = true)
  List<ProductAdminView> searchForAdminView(String keyword, List<Long> categoryIds,
                                            int categoryCount, String status, boolean isDeleted);

  @Modifying
  @Query(value = "UPDATE products SET is_deleted = false WHERE id = :id", nativeQuery = true)
  void restoreById(Long id);

  @Query(value = "SELECT * FROM products WHERE id = :id", nativeQuery = true)
  Optional<Product> findByIdWithDeleted(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.id IN :ids")
  List<Product> findAllByIdWithLock(@Param("ids") List<Long> ids);
}
