package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReviewService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(ProductUIController.class)
@WithAnonymousUser
@SuppressWarnings("unused")
public class ProductUIControllerTest {

  // Beans for SecurityConfig dependencies
  @MockitoBean
  private JpaUserDetailsService jpaUserDetailsService;
  @MockitoBean
  private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  @MockitoBean
  private CustomAccessDeniedHandler customAccessDeniedHandler;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ProductService productService;

  @MockitoBean
  private CategoryService categoryService;

  @MockitoBean
  private ReviewService reviewService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  @Test
  void showProductList_whenCalled_returnsProductListPage() throws Exception {
    Page<Product> productPage = new PageImpl<>(Collections.singletonList(new Product()));
    List<Category> categories = Collections.singletonList(new Category("Electronics"));

    when(productService.searchProducts(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(productPage);
    when(categoryService.findAllSortedByName()).thenReturn(categories);

    mockMvc.perform(get("/products/list"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/products-list"))
        .andExpect(model().attributeExists("productPage", "categories", "returnUrl"))
        .andExpect(model().attribute("productPage", productPage))
        .andExpect(model().attribute("categories", categories));
  }

  @Test
  void showProductList_withAllSearchParams_returnsProductListPage() throws Exception {
    Page<Product> productPage = new PageImpl<>(Collections.emptyList());

    when(productService.searchProducts(
        anyString(), anyList(), anyDouble(), anyDouble(), anyBoolean(), any(Pageable.class)))
        .thenReturn(productPage);
    when(categoryService.findAllSortedByName()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/products/list")
            .param("name", "Test")
            .param("categoryIds", "1", "2")
            .param("minPrice", "10.0")
            .param("maxPrice", "100.0")
            .param("onlyAvailable", "true")
            .param("page", "1")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/products-list"))
        .andExpect(model().attribute("searchName", "Test"))
        .andExpect(model().attribute("selectedCategoryIds", hasSize(2)))
        .andExpect(model().attribute("minPrice", 10.0))
        .andExpect(model().attribute("maxPrice", 100.0))
        .andExpect(model().attribute("onlyAvailable", true));
  }

  @Test
  void showProductList_withInvalidPageParam_throwsConstraintViolationException() throws Exception {
    mockMvc.perform(get("/products/list").param("page", "-1"))
        .andExpect(result -> {
          Exception resolvedException = result.getResolvedException();
          assertThat(resolvedException).isInstanceOf(ConstraintViolationException.class);
        });
  }

  @Test
  void productDetail_whenProductExists_returnsDetailPage() throws Exception {
    Product product = new Product();
    Page<com.ecommerce.dto.ReviewDto> reviewsPage = new PageImpl<>(Collections.emptyList());

    when(productService.findById(1L)).thenReturn(Optional.of(product));
    when(reviewService.getReviewsForProduct(any(Long.class), any(Pageable.class)))
        .thenReturn(reviewsPage);

    mockMvc.perform(get("/products/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/product-detail"))
        .andExpect(model().attributeExists("product", "reviewsPage", "newReview"))
        .andExpect(model().attribute("product", product));
  }

  @Test
  void productDetail_whenProductNotFound_throwsResourceNotFoundException() throws Exception {
    when(productService.findById(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/products/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/products/list"))
        .andExpect(flash().attribute("errorMessage", "Product with ID 1 not found."));
  }
}
