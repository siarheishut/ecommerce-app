package com.ecommerce.service;

import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  @Override
  @Transactional
  public void save(ProductDto productDto) {
    Product product;
    if (productDto.getId() == null) {
      product = new Product();
    } else {
      product = productRepository.findById(productDto.getId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Product with ID " + productDto.getId() + " not found."));
    }

    product.setName(productDto.getName());
    product.setDescription(productDto.getDescription());
    product.setPrice(productDto.getPrice());
    product.setStockQuantity(productDto.getStockQuantity());

    product.clearCategories();
    if (productDto.getCategories() != null) {
      categoryRepository.findAllById(productDto.getCategories()).forEach(product::addCategory);
    }

    productRepository.save(product);
  }

  @Override
  public void deleteById(Long id) {
    productRepository.deleteById(id);
  }

  @Override
  public Optional<Product> findById(Long id) {
    return productRepository.findById(id);
  }

  @Override
  public List<ProductAdminView> findAllForAdminList() {
    return productRepository.findAllForAdminView();
  }

  @Override
  @Transactional
  public void restoreById(Long id) {
    productRepository.restoreById(id);
  }

  @Override
  public Page<Product> searchProducts(
      String name, List<Long> categoryIds, Double minPrice, Double maxPrice, Boolean onlyAvailable, Pageable pageable) {
    Specification<Product> specification = Specification.unrestricted();

    if (name != null && !name.isBlank()) {
      specification = specification.and(ProductSpecification.hasName(name));
    }
    if (categoryIds != null && !categoryIds.isEmpty()) {
      specification = specification.and(ProductSpecification.inCategories(categoryIds));
    }
    if (minPrice != null || maxPrice != null) {
      if (minPrice == null) {
        minPrice = 0D;
      }
      if (maxPrice == null) {
        maxPrice = (double) Integer.MAX_VALUE;
      }
      specification = specification.and(ProductSpecification.inPriceRange(minPrice, maxPrice));
    }
    if (onlyAvailable != null && onlyAvailable) {
      specification = specification.and(ProductSpecification.isAvailable());
    }

    return productRepository.findAll(specification, pageable);
  }
}
