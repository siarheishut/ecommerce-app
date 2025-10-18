package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.CategoryInUseException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.RestoringActiveResourceException;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categoryRepository;

  @Override
  public List<Category> findAllForAdmin(String status) {
    boolean isDeleted = !"active".equals(status);
    return categoryRepository.findAllWithDeleted(status, isDeleted);
  }

  @Override
  public List<Category> findAllSortedByName() {
    return categoryRepository.findAllByOrderByNameAsc();
  }

  @Override
  public List<Category> searchByNameForAdmin(String keyword, String status) {
    boolean isDeleted = !"active".equals(status);
    return categoryRepository.searchByNameForAdmin(keyword, status, isDeleted);
  }

  @Override
  public Optional<Category> findById(Long id) {
    return categoryRepository.findById(id);
  }

  @Override
  @Transactional
  public void save(CategoryDto categoryDto) {
    final String categoryName = categoryDto.getName();

    Optional<Category> existingActiveCategory = categoryRepository.findByNameIgnoreCase(categoryName);
    if (existingActiveCategory.isPresent()) {
      if (categoryDto.getId() == null || !existingActiveCategory.get().getId().equals(categoryDto.getId())) {
        throw new DataIntegrityViolationException("A category with this name already exists.");
      }
    }

    Category category;
    if (categoryDto.getId() == null) {
      category = new Category(categoryName);
    } else {
      category = categoryRepository.findById(categoryDto.getId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Category with ID " + categoryDto.getId() + " not found."));
      category.setName(categoryName);
    }
    categoryRepository.save(category);
  }

  @Override
  @Transactional
  public void deleteById(Long id) {
    Category category = categoryRepository.findByIdWithDeleted(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + id + " not found."));

    if (!category.getProducts().isEmpty()) {
      throw new CategoryInUseException(
          "Cannot delete category '" + category.getBaseName() + "' because it is assigned to one or more products.");
    }

    String originalName = category.getBaseName();
    category.setName(originalName + "_deleted_" + Instant.now().toString());
    category.setDeleted(true);
    categoryRepository.save(category);
  }

  @Override
  @Transactional
  public void restoreById(Long id) {
    Category categoryToRestore = categoryRepository.findByIdWithDeleted(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category with ID " + id + " not found."));

    String originalName = categoryToRestore.getBaseName();

    if (categoryToRestore.getBaseName().equals(categoryToRestore.getName())) {
      throw new RestoringActiveResourceException("Cannot restore an active category with ID " + id + ".");
    }

    if (categoryRepository.findByNameIgnoreCase(originalName).isPresent()) {
      throw new DataIntegrityViolationException("An active category with the name '" + originalName + "' already exists.");
    }

    categoryToRestore.setName(originalName);
    categoryToRestore.setDeleted(false);
    categoryRepository.save(categoryToRestore);
  }
}
