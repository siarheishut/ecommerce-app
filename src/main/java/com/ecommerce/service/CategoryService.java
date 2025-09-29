package com.ecommerce.service;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
  List<Category> findAllSortedByName();

  Optional<Category> findById(Long id);

  void save(CategoryDto categoryDto);

  void deleteById(Long id);

  void restoreById(Long id);

  List<Category> findAllForAdmin();
}
