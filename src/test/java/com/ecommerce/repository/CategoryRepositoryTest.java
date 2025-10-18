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
  void whenFindAllWithDeleted_withStatusAll_returnsAllCategories() {
    categoryRepository.deleteById(cat2.getId());
    entityManager.flush();
    entityManager.clear();

    List<Category> foundCategories = categoryRepository.findAllWithDeleted("all", false);

    assertThat(foundCategories).hasSize(2);
    assertThat(foundCategories).containsExactlyInAnyOrder(cat1, cat2);
  }

  @Test
  void whenFindAllWithDeleted_withStatusActive_returnsOnlyActive() {
    categoryRepository.deleteById(cat2.getId());
    entityManager.flush();
    entityManager.clear();

    List<Category> foundCategories = categoryRepository.findAllWithDeleted("active", false);

    assertThat(foundCategories).hasSize(1);
    assertThat(foundCategories.getFirst().getName()).isEqualTo("Category_1");
  }

  @Test
  void whenFindAllWithDeleted_withStatusDeleted_returnsOnlyDeleted() {
    categoryRepository.deleteById(cat2.getId());
    entityManager.flush();
    entityManager.clear();

    List<Category> foundCategories = categoryRepository.findAllWithDeleted("deleted", true);

    assertThat(foundCategories).hasSize(1);
    assertThat(foundCategories.getFirst().getName()).isEqualTo("Category_2");
  }

  @Test
  void whenSearchByNameForAdmin_withKeywordAndStatusAll_returnsMatchingCategories() {
    categoryRepository.deleteById(cat2.getId());
    entityManager.flush();
    entityManager.clear();

    Category cat3 = new Category("Another_Category_1");
    entityManager.persist(cat3);

    List<Category> foundCategories = categoryRepository.searchByNameForAdmin("Category", "all", true);

    assertThat(foundCategories).hasSize(3);
    assertThat(foundCategories).extracting(Category::getBaseName)
        .containsExactlyInAnyOrder("Category_1", "Category_2", "Another_Category_1");
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
