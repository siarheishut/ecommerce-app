package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private ProductServiceImpl productService;

  @Test
  public void whenSave_withInvalidId_throwResourceNotFoundException() {
    Long invalidProductId = 10L;
    ProductDto productDto = new ProductDto();
    productDto.setId(invalidProductId);
    productDto.setName("Test Product");

    when(productRepository.findById(invalidProductId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> productService.save(productDto)
    );

    assertThat(exception.getMessage()).isEqualTo("Product with ID 10 not found.");
    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  public void whenSaveExistingProduct_withValidData_updateSuccessfully() {
    Long productId = 1L;
    ProductDto productDto = new ProductDto();
    productDto.setId(productId);
    productDto.setName("New");
    productDto.setPrice(new BigDecimal("100.0"));
    productDto.setStockQuantity(5);

    Product existingProduct = new Product();
    existingProduct.setName("Old");
    existingProduct.setPrice(new BigDecimal("50.0"));
    existingProduct.setStockQuantity(0);

    when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

    productService.save(productDto);
    verify(productRepository).save(productCaptor.capture());

    Product savedProduct = productCaptor.getValue();
    assertThat(savedProduct.getName()).isEqualTo("New");
    assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("100.0"));
    assertThat(savedProduct.getStockQuantity()).isEqualTo(5);
  }

  @Test
  public void whenSaveNewProduct_withCategories_categoriesAreAssociated() {
    ProductDto productDto = new ProductDto();
    productDto.setName("New");
    productDto.setPrice(new BigDecimal("100.0"));
    productDto.setStockQuantity(5);
    productDto.setCategories(List.of(5L, 10L));

    List<Category> categories = List.of(new Category("Furniture"), new Category("Toys"));
    when(categoryRepository.findAllById(productDto.getCategories())).thenReturn(categories);

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

    productService.save(productDto);
    verify(productRepository).save(productCaptor.capture());

    Product savedProduct = productCaptor.getValue();
    assertThat(savedProduct.getId()).isNull();
    assertThat(savedProduct.getName()).isEqualTo("New");
    assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("100.0"));
    assertThat(savedProduct.getStockQuantity()).isEqualTo(5);
    assertThat(savedProduct.getCategories()).containsExactlyInAnyOrderElementsOf(categories);
  }

  @Test
  public void whenSearchProducts_withNameFilter_ReturnFilteredPage() {
    String nameFilter = "Table";
    PageRequest pageable = PageRequest.of(0, 10);

    Product product1 = new Product();
    product1.setName("Green Table");
    Product product3 = new Product();
    product3.setName("Black Table");
    List<Product> expectedProducts = List.of(product1, product3);

    Page<Product> expectedPage =
        new PageImpl<>(expectedProducts, pageable, expectedProducts.size());

    when(productRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expectedPage);

    Page<Product> actualPage =
        productService.searchProducts(nameFilter, null, null, null, null, pageable);

    assertThat(actualPage.getContent()).containsExactlyInAnyOrder(product1, product3);
  }

  @Test
  public void whenSearchProducts_withCategoryFilter_thenReturnFilteredPage() {
    List<Long> categoryIds = List.of(1L);
    PageRequest pageable = PageRequest.of(0, 10);

    Product product1 = new Product();
    product1.setName("Product in Category 1");
    List<Product> expectedProducts = List.of(product1);
    Page<Product> expectedPage =
        new PageImpl<>(expectedProducts, pageable, expectedProducts.size());

    when(productRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expectedPage);

    Page<Product> actualPage = productService.searchProducts(
        null, categoryIds, null, null, null, pageable);

    assertThat(actualPage.getContent()).containsExactlyElementsOf(expectedProducts);
  }

  @Test
  public void whenSearchProducts_withPriceRangeFilter_thenReturnFilteredPage() {
    Double minPrice = 100.0;
    Double maxPrice = 200.0;
    PageRequest pageable = PageRequest.of(0, 10);

    Product product1 = new Product();
    product1.setName("Product in price range");
    product1.setPrice(new BigDecimal("150.00"));
    List<Product> expectedProducts = List.of(product1);
    Page<Product> expectedPage =
        new PageImpl<>(expectedProducts, pageable, expectedProducts.size());

    when(productRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expectedPage);

    Page<Product> actualPage =
        productService.searchProducts(null, null, minPrice, maxPrice, null, pageable);

    assertThat(actualPage.getContent()).containsExactlyElementsOf(expectedProducts);
  }

  @Test
  public void whenSearchProducts_withAvailabilityFilter_thenReturnFilteredPage() {
    Boolean onlyAvailable = true;
    PageRequest pageable = PageRequest.of(0, 10);

    Product product1 = new Product();
    product1.setName("Available Product");
    product1.setStockQuantity(10);
    List<Product> expectedProducts = List.of(product1);
    Page<Product> expectedPage =
        new PageImpl<>(expectedProducts, pageable, expectedProducts.size());

    when(productRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expectedPage);

    Page<Product> actualPage =
        productService.searchProducts(null, null, null, null, onlyAvailable, pageable);

    assertThat(actualPage.getContent()).containsExactlyElementsOf(expectedProducts);
  }

  @Test
  public void whenSearchProducts_withNoFilters_returnAllProducts() {
    PageRequest pageable = PageRequest.of(0, 10);

    Product product1 = new Product();
    Product product2 = new Product();

    List<Product> expectedProducts = List.of(product1, product2);
    Page<Product> expectedPage =
        new PageImpl<>(expectedProducts, pageable, expectedProducts.size());

    when(productRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expectedPage);
    Page<Product> actualPage =
        productService.searchProducts(null, null, null, null, null, pageable);

    assertThat(actualPage.getContent()).containsExactlyInAnyOrderElementsOf(expectedProducts);
  }

  @Test
  public void whenSearchProducts_withAllFieldsFilter_returnFilteredPage() {
    String nameFilter = "Table";
    PageRequest pageable = PageRequest.of(0, 10);

    Product product1 = new Product();
    product1.setName("Green Table");
    product1.addCategory(new Category("Furniture"));
    product1.setPrice(new BigDecimal("1000.0"));
    product1.setStockQuantity(15);
    Product product2 = new Product();
    product2.addCategory(new Category("Furniture"));
    product2.setName("Yellow Table");
    product2.setPrice(new BigDecimal("199.0"));
    product2.setStockQuantity(2);
    Product product3 = new Product();
    product3.addCategory(new Category("Furniture"));
    product3.setName("Black Table");
    product3.setPrice(new BigDecimal("250.0"));
    product3.setStockQuantity(2);
    Product product4 = new Product();
    product4.addCategory(new Category("Furniture"));
    product4.setName("Golden Table");
    product4.setPrice(new BigDecimal("300.0"));
    product4.setStockQuantity(0);
    Product product5 = new Product();
    product5.addCategory(new Category("Toys"));
    product5.setName("Crystal table");
    product5.setPrice(new BigDecimal("900.0"));
    product5.setStockQuantity(7);

    List<Product> expectedProducts = List.of(product1, product3);

    Page<Product> expectedPage =
        new PageImpl<>(expectedProducts, pageable, expectedProducts.size());

    when(productRepository.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(expectedPage);

    Page<Product> actualPage = productService.searchProducts(
        nameFilter, List.of(1L), 200.0, 1000.0, true, pageable);

    assertThat(actualPage.getContent()).containsExactlyInAnyOrderElementsOf(expectedProducts);
  }

  @Test
  public void whenDeleteById_deleteSuccessfully() {
    Long productId = 1L;
    productService.deleteById(productId);
    verify(productRepository).deleteById(productId);
  }

  @Test
  public void whenFindById_foundSuccessfully() {
    Long productId = 1L;
    Product expectedProduct = new Product();
    when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

    Optional<Product> actualProduct = productService.findById(productId);

    assertThat(actualProduct).isPresent().contains(expectedProduct);
    verify(productRepository).findById(productId);
  }

  @Test
  public void whenFindAllForAdminList_RepositoryMethodIsCalled() {
    List<ProductAdminView> expectedList = List.of(mock(ProductAdminView.class));
    when(productRepository.findAllForAdminView()).thenReturn(expectedList);

    List<ProductAdminView> actualList = productService.findAllForAdminList();

    assertThat(actualList).isEqualTo(expectedList);
    verify(productRepository).findAllForAdminView();
  }

  @Test
  public void whenRestoreById_RepositoryMethodIsCalled() {
    Long productId = 1L;
    productService.restoreById(productId);
    verify(productRepository).restoreById(productId);
  }
}
