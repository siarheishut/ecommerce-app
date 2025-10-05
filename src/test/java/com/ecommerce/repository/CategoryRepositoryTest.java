package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
public class CategoryRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private CategoryRepository categoryRepository;

  private Category cat1;
  private Category cat2;

  @BeforeEach
  void setUp() {
    cat1 = new Category("Category_1");
    cat2 = new Category("Category_2");
    entityManager.persist(cat1);
    entityManager.persist(cat2);
  }

  @Test
  void whenFindByNameIgnoreCase_withExistingActiveCategory_returnsOptionalOfCategory() {
    Optional<Category> foundCategory = categoryRepository.findByNameIgnoreCase("category_1");

    assertThat(foundCategory).isPresent();
    assertThat(foundCategory.get().getName()).isEqualTo("Category_1");
  }

  @Test
  void whenFindByNameIgnoreCase_withSoftDeletedCategory_returnsEmptyOptional() {
    categoryRepository.delete(cat1);

    Optional<Category> foundCategory = categoryRepository.findByNameIgnoreCase("category_1");

    assertThat(foundCategory).isNotPresent();
  }

  @Test
  void whenFindAllWithDeleted_withActiveAndDeletedCategories_returnsAllCategories() {
    categoryRepository.deleteById(cat2.getId());

    List<Category> foundCategories = categoryRepository.findAllWithDeleted();

    assertThat(foundCategories).hasSize(2);
    assertThat(foundCategories).containsExactlyInAnyOrder(cat1, cat2);
  }

  @Test
  void whenFindByIdWithDeleted_withActiveCategory_returnsOptionalOfCategory() {
    Optional<Category> foundCategory = categoryRepository.findByIdWithDeleted(cat1.getId());

    assertThat(foundCategory).isPresent();
    assertThat(foundCategory.get().getName()).isEqualTo("Category_1");
    assertThat(foundCategory.get().isDeleted()).isFalse();
  }

  @Test
  void whenFindByIdWithDeleted_withSoftDeletedCategory_returnsOptionalOfCategory() {
    categoryRepository.delete(cat1);
    entityManager.flush();
    entityManager.clear();

    Optional<Category> foundCategory = categoryRepository.findByIdWithDeleted(cat1.getId());

    assertThat(foundCategory).isPresent();
    assertThat(foundCategory.get().getName()).isEqualTo("Category_1");
    assertThat(foundCategory.get().isDeleted()).isTrue();
  }

  @Test
  void whenSaveAndFlush_withDuplicateName_throwsDataIntegrityViolationException() {
    Category duplicateCategory = new Category("Category_1");

    assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicateCategory))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void whenSaveAndFlush_withBlankName_throwsConstraintViolationException() {
    Category categoryWithBlankName = new Category("   ");

    assertThatThrownBy(() -> categoryRepository.saveAndFlush(categoryWithBlankName))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Category name is required.");
  }
}
