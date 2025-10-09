package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CategoryInUseException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.RestoringActiveResourceException;
import com.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryServiceImpl categoryService;

  @Test
  void whenFindAllForAdmin_findSuccessfully() {
    List<Category> expectedCategories =
        List.of(new Category("Furniture"), new Category("Toys"));
    when(categoryRepository.findAllWithDeleted()).thenReturn(expectedCategories);

    List<Category> actualCategories = categoryService.findAllForAdmin();

    assertThat(actualCategories).isEqualTo(expectedCategories);
    verify(categoryRepository).findAllWithDeleted();
  }

  @Test
  void whenFindAllSortedByName_findSuccessfully() {
    List<Category> expectedCategories =
        List.of(new Category("Toys"), new Category("Furniture"));
    when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(expectedCategories);

    List<Category> actualCategories = categoryService.findAllSortedByName();

    assertThat(actualCategories).isEqualTo(expectedCategories);
    verify(categoryRepository).findAllByOrderByNameAsc();
  }

  @Test
  void whenFindById_findSuccessfully() {
    Long categoryId = 1L;
    Optional<Category> expectedCategory = Optional.of(new Category("Test"));
    when(categoryRepository.findById(categoryId)).thenReturn(expectedCategory);

    Optional<Category> actualCategory = categoryService.findById(categoryId);

    assertThat(actualCategory).isEqualTo(expectedCategory);
    verify(categoryRepository).findById(categoryId);
  }

  @Test
  void whenSaveNewCategory_withUniqueName_savesSuccessfully() {
    CategoryDto categoryDto = new CategoryDto();
    categoryDto.setName("Games");

    when(categoryRepository.findByNameIgnoreCase("Games")).thenReturn(Optional.empty());

    categoryService.save(categoryDto);

    ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
    verify(categoryRepository).save(categoryCaptor.capture());

    Category savedCategory = categoryCaptor.getValue();
    assertThat(savedCategory.getName()).isEqualTo("Games");
    assertThat(savedCategory.getId()).isNull();
  }

  @Test
  void whenSaveNewCategory_withExistingName_throwsDataIntegrityViolationException() {
    CategoryDto categoryDto = new CategoryDto();
    categoryDto.setName("Gadgets");

    when(categoryRepository.findByNameIgnoreCase("Gadgets"))
        .thenReturn(Optional.of(new Category()));

    DataIntegrityViolationException exception = assertThrows(
        DataIntegrityViolationException.class,
        () -> categoryService.save(categoryDto)
    );
    verify(categoryRepository, never()).save(any(Category.class));
    assertThat(exception.getMessage()).isEqualTo("A category with this name already exists.");
  }

  @Test
  void whenSaveExistingCategory_withValidData_updatesSuccessfully() {
    CategoryDto categoryDto = new CategoryDto();
    categoryDto.setId(1L);
    categoryDto.setName("New Name");

    Category existingCategory = new Category("Old Name");
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.findByNameIgnoreCase("New Name")).thenReturn(Optional.empty());

    categoryService.save(categoryDto);

    ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
    verify(categoryRepository).save(categoryCaptor.capture());

    Category savedCategory = categoryCaptor.getValue();
    assertThat(savedCategory.getName()).isEqualTo("New Name");
  }

  @Test
  void whenSaveExistingCategory_withConflictingName_throwsDataIntegrityViolationException() {
    CategoryDto categoryDto = new CategoryDto();
    categoryDto.setId(1L);
    categoryDto.setName("Some Name");

    Category otherCategory = mock(Category.class);
    when(otherCategory.getId()).thenReturn(2L);

    when(categoryRepository.findByNameIgnoreCase("Some Name"))
        .thenReturn(Optional.of(otherCategory));

    DataIntegrityViolationException exception = assertThrows(
        DataIntegrityViolationException.class,
        () -> categoryService.save(categoryDto)
    );
    verify(categoryRepository, never()).save(any(Category.class));
    assertThat(exception.getMessage()).isEqualTo("A category with this name already exists.");
  }

  @Test
  void whenSaveExistingCategory_withNonExistentId_throwsResourceNotFoundException() {
    CategoryDto categoryDto = new CategoryDto();
    categoryDto.setId(1L);
    categoryDto.setName("Some Name");

    when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
    when(categoryRepository.findByNameIgnoreCase("Some Name")).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> categoryService.save(categoryDto)
    );
    verify(categoryRepository, never()).save(any(Category.class));
    assertThat(exception.getMessage()).isEqualTo("Category with ID 1 not found.");
  }

  @Test
  void whenDeleteById_withUnusedCategory_softDeletesSuccessfully() {
    Long categoryId = 1L;
    Category category = new Category("Furniture");

    when(categoryRepository.findByIdWithDeleted(categoryId)).thenReturn(Optional.of(category));

    categoryService.deleteById(categoryId);

    ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
    verify(categoryRepository).save(categoryCaptor.capture());

    Category savedCategory = categoryCaptor.getValue();
    assertThat(savedCategory.isDeleted()).isTrue();
    assertThat(savedCategory.getName()).startsWith("Furniture_deleted_");
  }

  @Test
  void whenDeleteById_withUsedCategory_throwsCategoryInUseException() {
    Long categoryId = 1L;
    Category category = spy(new Category("Furniture"));
    Product product = new Product();

    when(category.getProducts()).thenReturn(List.of(product));
    when(categoryRepository.findByIdWithDeleted(categoryId)).thenReturn(Optional.of(category));

    CategoryInUseException exception = assertThrows(
        CategoryInUseException.class,
        () -> categoryService.deleteById(categoryId)
    );
    verify(categoryRepository, never()).save(any(Category.class));
    assertThat(exception.getMessage()).isEqualTo(
        "Cannot delete category 'Furniture' because it is assigned to one or more products.");
  }

  @Test
  void whenDeleteById_withNonExistentId_throwsResourceNotFoundException() {
    Long categoryId = 1L;
    when(categoryRepository.findByIdWithDeleted(categoryId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteById(categoryId));
    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void whenRestoreById_withNoNameConflict_restoresSuccessfully() {
    Long categoryId = 1L;
    Category category = new Category("Furniture_deleted_123");
    category.setDeleted(true);

    when(categoryRepository.findByIdWithDeleted(categoryId)).thenReturn(Optional.of(category));
    when(categoryRepository.findByNameIgnoreCase("Furniture")).thenReturn(Optional.empty());

    categoryService.restoreById(categoryId);

    ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
    verify(categoryRepository).save(categoryCaptor.capture());

    Category restoredCategory = categoryCaptor.getValue();
    assertThat(restoredCategory.isDeleted()).isFalse();
    assertThat(restoredCategory.getName()).isEqualTo("Furniture");
  }

  @Test
  void whenRestoreById_withNameConflict_throwsDataIntegrityViolationException() {
    Long categoryId = 1L;
    Category categoryToRestore = new Category("Furniture_deleted_123");
    categoryToRestore.setDeleted(true);

    when(categoryRepository.findByIdWithDeleted(categoryId))
        .thenReturn(Optional.of(categoryToRestore));
    when(categoryRepository.findByNameIgnoreCase("Furniture"))
        .thenReturn(Optional.of(new Category()));

    DataIntegrityViolationException exception = assertThrows(
        DataIntegrityViolationException.class,
        () -> categoryService.restoreById(categoryId)
    );
    verify(categoryRepository, never()).save(any(Category.class));
    assertThat(exception.getMessage()).isEqualTo(
        "An active category with the name 'Furniture' already exists.");
  }

  @Test
  void whenRestoreById_withNonExistentId_throwsResourceNotFoundException() {
    Long categoryId = 1L;
    when(categoryRepository.findByIdWithDeleted(categoryId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> categoryService.restoreById(categoryId)
    );
    verify(categoryRepository, never()).save(any(Category.class));
    assertThat(exception.getMessage()).isEqualTo("Category with ID 1 not found.");
  }

  @Test
  void whenRestoreById_withActiveCategory_throwsRestoringActiveResourceException() {
    Long categoryId = 1L;
    Category activeCategory = new Category("Furniture");

    when(categoryRepository.findByIdWithDeleted(categoryId)).thenReturn(Optional.of(activeCategory));

    RestoringActiveResourceException exception = assertThrows(
        RestoringActiveResourceException.class,
        () -> categoryService.restoreById(categoryId)
    );

    assertThat(exception.getMessage()).isEqualTo("Cannot restore an active category with ID 1.");
    verify(categoryRepository, never()).save(any(Category.class));
  }
}
