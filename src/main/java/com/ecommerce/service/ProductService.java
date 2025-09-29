package com.ecommerce.service;

import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    void save(ProductDto productDto);

    void deleteById(Long id);

    Optional<Product> findById(Long id);

    Page<Product> searchProducts(String name, List<Long> categoryIds,
                                 Double minPrice, Double maxPrice, Boolean onlyAvailable, Pageable pageable);

    List<ProductAdminView> findAllForAdminList();

    void restoreById(Long id);
}
