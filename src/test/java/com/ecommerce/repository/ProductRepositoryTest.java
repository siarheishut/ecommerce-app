package com.ecommerce.repository;

import com.ecommerce.AbstractIntegrationTest;
import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest extends AbstractIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ProductRepository productRepository;

  @Test
  void whenFindById_withExistingActiveProduct_returnsOptionalOfProduct() {
    Product product = new Product();
    product.setName("Toy");
    product.setStockQuantity(1);
    product.setPrice(BigDecimal.ONE);
    Product savedProduct = entityManager.persistAndFlush(product);

    Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

    assertThat(foundProduct).isPresent();
    assertThat(foundProduct.get().getName()).isEqualTo("Toy");
  }

  @Test
  void whenFindById_withSoftDeletedProduct_returnsEmptyOptional() {
    Product product = new Product();
    product.setName("Toy");
    product.setStockQuantity(1);
    product.setPrice(BigDecimal.ONE);
    entityManager.persistAndFlush(product);

    productRepository.deleteById(product.getId());

    Optional<Product> foundProduct = productRepository.findById(product.getId());
    assertThat(foundProduct).isNotPresent();
  }

  @Test
  void whenFindById_withRestoredProduct_returnsOptionalOfProduct() {
    Product product = new Product();
    product.setName("Toy");
    product.setStockQuantity(1);
    product.setPrice(BigDecimal.ONE);
    entityManager.persistAndFlush(product);

    productRepository.deleteById(product.getId());
    productRepository.restoreById(product.getId());

    Optional<Product> foundProduct = productRepository.findById(product.getId());
    assertThat(foundProduct).isPresent();
  }

  @Test
  void whenFindByIdWithDeleted_withActiveProduct_returnsOptionalOfProduct() {
    Product product = new Product();
    product.setName("Active Product");
    product.setStockQuantity(10);
    product.setPrice(BigDecimal.TEN);
    Product savedProduct = entityManager.persistAndFlush(product);

    Optional<Product> foundProduct = productRepository.findByIdWithDeleted(savedProduct.getId());

    assertThat(foundProduct).isPresent();
    assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
    assertThat(foundProduct.get().isDeleted()).isFalse();
  }

  @Test
  void whenFindByIdWithDeleted_withSoftDeletedProduct_returnsOptionalOfProduct() {
    Product product = new Product();
    product.setName("Deleted Product");
    product.setStockQuantity(10);
    product.setPrice(BigDecimal.TEN);
    Product savedProduct = entityManager.persistAndFlush(product);
    productRepository.deleteById(savedProduct.getId());

    Optional<Product> foundProduct = productRepository.findByIdWithDeleted(savedProduct.getId());

    assertThat(foundProduct).isPresent();
    assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
    assertThat(foundProduct.get().isDeleted()).isTrue();
  }

  @Test
  void whenFindAllForAdminView_withMixedProducts_returnsCorrectDtoList() {
    Category electronics = new Category("Electronics");
    Category books = new Category("Books");
    entityManager.persist(electronics);
    entityManager.persist(books);

    Product p1 = new Product();
    p1.setName("Laptop");
    p1.setPrice(new BigDecimal("1200.00"));
    p1.setStockQuantity(10);
    p1.addCategory(electronics);
    entityManager.persist(p1);

    Product p2 = new Product();
    p2.setName("E-Reader");
    p2.setPrice(new BigDecimal("250.00"));
    p2.setStockQuantity(20);
    p2.addCategory(electronics);
    p2.addCategory(books);
    entityManager.persist(p2);

    Product p3 = new Product();
    p3.setName("Old Phone");
    p3.setPrice(new BigDecimal("50.00"));
    p3.setStockQuantity(0);
    p3.setDeleted(true);
    entityManager.persist(p3);

    List<ProductAdminView> adminView = productRepository.findAllForAdminView();

    assertThat(adminView).hasSize(3);
    assertThat(adminView).extracting(ProductAdminView::getName)
        .containsExactly("E-Reader", "Laptop", "Old Phone");
    assertThat(adminView.get(0).getCategoriesString()).isEqualTo("Books, Electronics");
    assertThat(adminView.get(1).getCategoriesString()).isEqualTo("Electronics");
    assertThat(adminView.get(2).getCategoriesString()).isNull();
    assertThat(adminView.get(2).getIsDeleted()).isTrue();
  }

  @Test
  void whenSaveAndFlush_withNullName_throwsConstraintViolationException() {
    Product productWithNullName = new Product();
    productWithNullName.setName(null);
    productWithNullName.setPrice(BigDecimal.TEN);
    productWithNullName.setStockQuantity(1);

    assertThatThrownBy(() -> productRepository.saveAndFlush(productWithNullName))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Product name is required.");
  }

  @Test
  void whenSaveAndFlush_withNullPrice_throwsConstraintViolationException() {
    Product productWithNullPrice = new Product();
    productWithNullPrice.setName("Product");
    productWithNullPrice.setPrice(null);
    productWithNullPrice.setStockQuantity(1);

    assertThatThrownBy(() -> productRepository.saveAndFlush(productWithNullPrice))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Product price is required.");
  }

  @Test
  void whenSaveAndFlush_withDuplicateCategoryInSet_throwsDataIntegrityViolationException() {
    Category category = new Category("Electronics");
    entityManager.persist(category);

    Product product = new Product();
    product.setName("Laptop");
    product.setPrice(BigDecimal.TEN);
    product.setStockQuantity(1);
    product.addCategory(category);
    productRepository.saveAndFlush(product);

    product.addCategory(category);
    assertThatThrownBy(() -> productRepository.saveAndFlush(product))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
