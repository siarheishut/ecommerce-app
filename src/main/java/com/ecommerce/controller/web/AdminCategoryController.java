package com.ecommerce.controller.web;

import com.ecommerce.dto.CategoryDto;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.CategoryInUseException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Tag(name = "Admin Category Management", description = "Operations for managing categories.")
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
  private final CategoryService categoryService;

  @ModelAttribute("adminSection")
  public String adminSection() {
    return "categories";
  }

  @Operation(
      summary = "Show add category form",
      description = "Displays the form for creating a new category.")
  @ApiResponse(responseCode = "200", description = "Form displayed successfully.")
  @GetMapping("/add")
  public String showAddForm(Model model) {
    model.addAttribute("category", new CategoryDto());
    return "admin/category-form";
  }

  @Operation(
      summary = "Show edit category form",
      description = "Displays the form for editing an existing category.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Form displayed with category data."),
      @ApiResponse(responseCode = "302", description = "Redirects to list if category not found.")
  })
  @GetMapping("/edit/{id}")
  public String showUpdateForm(
      @Parameter(description = "ID of the category to edit")
      @PathVariable Long id,

      Model model,
      RedirectAttributes redirectAttributes) {
    Optional<Category> category = categoryService.findById(id);
    if (category.isEmpty()) {
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Category with id " + id + " not found.");
      return "redirect:/admin/categories/list";
    }

    model.addAttribute("category",
        new CategoryDto(category.get().getId(), category.get().getName()));
    return "admin/category-form";
  }

  @Operation(
      summary = "Save category",
      description = "Creates a new category or updates an existing one.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Failure: Validation errors, returns form view."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to category list. <br>" +
              "• **Success:** Category saved. <br>" +
              "• **Failure:** Category ID not found for update.")
  })
  @PostMapping("/save")
  public String saveCategory(@Valid @ModelAttribute("category") CategoryDto categoryDto,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      log.warn("Category form has validation errors.");
      return "admin/category-form";
    }
    try {
      categoryService.save(categoryDto);
      redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully.");
    } catch (DataIntegrityViolationException e) {
      log.warn("Validation error while saving category with name '{}'", categoryDto.getName(), e);
      bindingResult.rejectValue(
          "name", "category.name", "A category with this name already exists.");
      return "admin/category-form";
    } catch (ResourceNotFoundException e) {
      log.warn("Attempted to save a category that does not exist with id: {}",
          categoryDto.getId(), e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/admin/categories/list";
  }

  @Operation(
      summary = "List categories",
      description = "Displays a list of categories with optional filtering.")
  @ApiResponse(responseCode = "200", description = "List displayed successfully.")
  @GetMapping("/list")
  public String listCategories(
      @Parameter(description = "Search keyword.")
      @RequestParam(value = "keyword", required = false) String keyword,

      @Parameter(description = "Filter by status.")
      @RequestParam(value = "status", defaultValue = "all") String status,

      Model model) {
    List<Category> categories;
    if (keyword != null && !keyword.trim().isEmpty()) {
      categories = categoryService.searchByNameForAdmin(keyword, status);
      model.addAttribute("keyword", keyword);
    } else {
      categories = categoryService.findAllForAdmin(status);
    }
    model.addAttribute("status", status);
    model.addAttribute("categories", categories);
    return "admin/categories-list";
  }

  @Operation(summary = "Delete category", description = "Deletes a category if it is not in use.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to referer page. <br>" +
              "• **Success:** Category deleted. <br>" +
              "• **Failure:** Category in use, not found, or internal error.")
  })
  @DeleteMapping("/delete/{id}")
  public String deleteCategories(
      @Parameter(description = "ID of the category to delete.")
      @PathVariable Long id,

      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    try {
      categoryService.deleteById(id);
      redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
    } catch (CategoryInUseException e) {
      log.warn("Attempted to delete a category in use with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (ResourceNotFoundException e) {
      log.warn("Attempted to delete a non-existent category with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error deleting category with id: {}", id, e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An error occurred while deleting the category.");
    }
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/admin/categories/list");
  }

  @Operation(summary = "Restore category", description = "Restores a deleted category.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to referer page. <br>" +
              "• **Success:** Category restored. <br>" +
              "• **Failure:** Duplicate name, not found, or internal error.")
  })
  @PostMapping("/restore/{id}")
  public String restoreCategory(
      @Parameter(description = "ID of the category to restore.")
      @PathVariable Long id,

      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    try {
      categoryService.restoreById(id);
      redirectAttributes.addFlashAttribute("successMessage", "Category restored successfully.");
    } catch (DataIntegrityViolationException e) {
      log.warn("Attempted to restore a category with a conflicting name for id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (ResourceNotFoundException e) {
      log.warn("Attempted to restore a non-existent category with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error restoring category with id: {}", id, e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An error occurred while restoring the category.");
    }
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/admin/categories/list");
  }
}
