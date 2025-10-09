package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(AdminProductController.class)
@WithMockUser(roles = "ADMIN")
@SuppressWarnings("unused")
public class AdminProductControllerTest {

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
  private StringToCategoryConverter stringToCategoryConverter;

  @MockitoBean
  private ProductService productService;

  @MockitoBean
  private CategoryService categoryService;

  @Test
  void whenShowAddForm_returnsFormView() throws Exception {
    when(categoryService.findAllSortedByName()).thenReturn(Collections.emptyList());
    mockMvc.perform(get("/admin/products/add"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/product-form"))
        .andExpect(model().attributeExists("product", "allCategories"));
  }

  @Test
  void whenShowUpdateForm_withValidId_returnsFormView() throws Exception {
    Product product = new Product();
    product.setName("Test Product");
    when(productService.findById(1L)).thenReturn(Optional.of(product));
    when(categoryService.findAllSortedByName()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/admin/products/edit/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/product-form"))
        .andExpect(model().attribute("product", hasProperty("name", is("Test Product"))))
        .andExpect(model().attributeExists("allCategories"));
  }

  @Test
  void whenShowUpdateForm_withInvalidId_redirectsToListWithErrorMessage() throws Exception {
    when(productService.findById(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/admin/products/edit/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("errorMessage", "Product not found with id: 1"));
  }

  @Test
  void whenSaveProduct_withValidData_redirectsToList() throws Exception {
    ArgumentCaptor<ProductDto> productDtoCaptor = ArgumentCaptor.forClass(ProductDto.class);

    mockMvc.perform(post("/admin/products/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "New Product")
            .param("price", "10.00")
            .param("stockQuantity", "100")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("successMessage", "Product saved successfully."));

    verify(productService).save(productDtoCaptor.capture());
    ProductDto capturedDto = productDtoCaptor.getValue();
    assertThat(capturedDto.getName()).isEqualTo("New Product");
    assertThat(capturedDto.getPrice()).isEqualByComparingTo("10.00");
    assertThat(capturedDto.getStockQuantity()).isEqualTo(100);
  }

  @Test
  void whenSaveProduct_withInvalidData_returnsFormView() throws Exception {
    when(categoryService.findAllSortedByName()).thenReturn(Collections.emptyList());

    mockMvc.perform(post("/admin/products/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/product-form"))
        .andExpect(model().attributeExists("allCategories"));
  }

  @Test
  void whenSaveProduct_withNonExistentId_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Product with ID 10 not found."))
        .when(productService).save(any(ProductDto.class));

    mockMvc.perform(post("/admin/products/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "10")
            .param("name", "Non Existent Product")
            .param("price", "10.00")
            .param("stockQuantity", "100")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("errorMessage", "Product with ID 10 not found."));
  }

  @Test
  void whenListProducts_returnsListView() throws Exception {
    when(productService.findAllForAdminList()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/admin/products/list"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/products-list"))
        .andExpect(model().attributeExists("products"));
  }

  @Test
  void whenDeleteProduct_withValidData_redirectsToList() throws Exception {
    mockMvc.perform(delete("/admin/products/delete/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("successMessage", "Product deleted successfully."));

    verify(productService).deleteById(1L);
  }

  @Test
  void whenDeleteProduct_withNonExistingId_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Product with ID 1 not found."))
        .when(productService).deleteById(anyLong());

    mockMvc.perform(delete("/admin/products/delete/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("errorMessage", "Product with ID 1 not found."));
  }

  @Test
  void whenRestoreProduct_withValidData_redirectsToList() throws Exception {
    mockMvc.perform(post("/admin/products/restore/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("successMessage", "Product restored successfully."));

    verify(productService).restoreById(1L);
  }

  @Test
  void whenRestoreProduct_withNonExistingId_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Product with ID 1 not found."))
        .when(productService).restoreById(anyLong());

    mockMvc.perform(post("/admin/products/restore/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/products/list"))
        .andExpect(flash().attribute("errorMessage", "Product with ID 1 not found."));
  }
}
