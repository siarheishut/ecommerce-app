package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.CategoryInUseException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(AdminCategoryController.class)
@WithMockUser(roles = "ADMIN")
@SuppressWarnings("unused")
public class AdminCategoryControllerTest {

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
  private CategoryService categoryService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  @Test
  void whenShowAddForm_returnsFormView() throws Exception {
    mockMvc.perform(get("/admin/categories/add"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/category-form"))
        .andExpect(model().attributeExists("category"));
  }

  @Test
  void whenShowUpdateForm_withValidId_returnsFormView() throws Exception {
    Category category = new Category();
    category.setName("Test Category");
    when(categoryService.findById(1L)).thenReturn(Optional.of(category));

    mockMvc.perform(get("/admin/categories/edit/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/category-form"))
        .andExpect(model().attribute("category", hasProperty("name", is("Test Category"))));
  }

  @Test
  void whenShowUpdateForm_withInvalidId_returnsNotFound() throws Exception {
    when(categoryService.findById(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/admin/categories/edit/1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("errorMessage", "Category with id 1 not found."));
  }

  @Test
  void whenSaveCategory_withValidData_redirectsToList() throws Exception {
    ArgumentCaptor<CategoryDto> categoryDtoCaptor = ArgumentCaptor.forClass(CategoryDto.class);

    mockMvc.perform(post("/admin/categories/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "New Category")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("successMessage", "Category saved successfully."));

    verify(categoryService).save(categoryDtoCaptor.capture());
    assertThat(categoryDtoCaptor.getValue().getName()).isEqualTo("New Category");
  }

  @Test
  void whenSaveCategory_withBlankName_returnsFormView() throws Exception {
    mockMvc.perform(post("/admin/categories/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "  ")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/category-form"))
        .andExpect(model().attributeHasFieldErrors("category", "name"));
  }

  @Test
  void whenSaveCategory_withDuplicateName_returnsFormViewWithError() throws Exception {
    doThrow(new DataIntegrityViolationException(""))
        .when(categoryService).save(any(CategoryDto.class));

    mockMvc.perform(post("/admin/categories/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Duplicate Category")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("admin/category-form"))
        .andExpect(model().attributeHasFieldErrors("category", "name"));
  }

  @Test
  void whenSaveCategory_whenNotFound_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Category with ID 10 not found."))
        .when(categoryService).save(any(CategoryDto.class));

    mockMvc.perform(post("/admin/categories/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "10")
            .param("name", "Non-existent Category")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("errorMessage", "Category with ID 10 not found."));
  }

  @Test
  void whenListCategories_withNoParams_returnsListViewWithAllCategories() throws Exception {
    List<Category> categories = List.of(new Category("Test"));
    when(categoryService.findAllForAdmin("all")).thenReturn(categories);

    mockMvc.perform(get("/admin/categories/list"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/categories-list"))
        .andExpect(model().attribute("categories", is(categories)))
        .andExpect(model().attribute("status", is("all")));

    verify(categoryService).findAllForAdmin("all");
  }

  @Test
  void whenListCategories_withKeyword_returnsListViewWithSearchResults() throws Exception {
    List<Category> categories = List.of(new Category("Searched"));
    when(categoryService.searchByNameForAdmin("key", "all")).thenReturn(categories);

    mockMvc.perform(get("/admin/categories/list").param("keyword", "key"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/categories-list"))
        .andExpect(model().attribute("categories", is(categories)))
        .andExpect(model().attribute("keyword", is("key")))
        .andExpect(model().attribute("status", is("all")));

    verify(categoryService).searchByNameForAdmin("key", "all");
  }

  @Test
  void whenListCategories_withStatus_returnsListViewWithFilteredCategories() throws Exception {
    mockMvc.perform(get("/admin/categories/list").param("status", "deleted"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/categories-list"))
        .andExpect(model().attribute("status", is("deleted")));

    verify(categoryService).findAllForAdmin("deleted");
  }

  @Test
  void whenDeleteCategory_withSuccess_redirectsToListWithSuccessMessage() throws Exception {
    mockMvc.perform(delete("/admin/categories/delete/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("successMessage", "Category deleted successfully."));

    verify(categoryService).deleteById(1L);
  }

  @Test
  void whenDeleteCategory_whenNotFound_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Category with ID 1 not found."))
        .when(categoryService).deleteById(1L);

    mockMvc.perform(delete("/admin/categories/delete/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("errorMessage", "Category with ID 1 not found."));
  }

  @Test
  void whenDeleteCategory_whenInUse_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new CategoryInUseException(
        "Cannot delete category 'category' because it is assigned to one or more products."))
        .when(categoryService).deleteById(1L);

    mockMvc.perform(delete("/admin/categories/delete/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("errorMessage",
            "Cannot delete category 'category' because it is assigned to one or more products."));
  }

  @Test
  void whenRestoreCategory_withSuccess_redirectsToListWithSuccessMessage() throws Exception {
    mockMvc.perform(post("/admin/categories/restore/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("successMessage", "Category restored successfully."));

    verify(categoryService).restoreById(1L);
  }

  @Test
  void whenRestoreCategory_withExistingName_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new DataIntegrityViolationException(
        "An active category with the name 'category' already exists.")).
        when(categoryService).restoreById(1L);

    mockMvc.perform(post("/admin/categories/restore/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("errorMessage",
            "An active category with the name 'category' already exists."));
  }

  @Test
  void whenRestoreCategory_whenNotFound_redirectsToListWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Category with ID 1 not found.")).
        when(categoryService).restoreById(1L);

    mockMvc.perform(post("/admin/categories/restore/1").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/categories/list"))
        .andExpect(flash().attribute("errorMessage", "Category with ID 1 not found."));
  }
}
