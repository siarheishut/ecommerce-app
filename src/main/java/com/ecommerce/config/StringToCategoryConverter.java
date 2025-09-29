package com.ecommerce.config;

import com.ecommerce.entity.Category;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StringToCategoryConverter implements Converter<String, Category> {
  private final CategoryRepository categoryRepository;

  @Override
  public Category convert(String source) {
    if (source.isBlank()) {
      throw new IllegalArgumentException("Category ID cannot be blank.");
    }

    try {
      long id = Long.parseLong(source);
      return categoryRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid category ID format: '" + source + "'", e);
    }
  }
}
