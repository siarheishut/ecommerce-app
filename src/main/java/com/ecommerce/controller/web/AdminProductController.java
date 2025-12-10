package com.ecommerce.controller.web;

import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Tag(name = "Admin Product Management", description = "Operations for managing products.")
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductController {
  private final ProductService productService;
  private final CategoryService categoryService;

  @ModelAttribute("adminSection")
  public String adminSection() {
    return "products";
  }

  @Operation(
      summary = "Show add product form",
      description = "Displays the form for creating a new product.")
  @ApiResponse(responseCode = "200", description = "Form displayed successfully.")
  @GetMapping("/add")
  public String showAddForm(Model model) {
    model.addAttribute("product", new ProductDto());
    model.addAttribute("allCategories", categoryService.findAllSortedByName());
    return "admin/product-form";
  }

  @Operation(
      summary = "Show edit product form",
      description = "Displays the form for editing an existing product.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Form displayed with product data."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to list if product not found.")
  })
  @GetMapping("/edit/{id}")
  public String showUpdateForm(
      @Parameter(description = "ID of the product to edit.")
      @PathVariable Long id,

      Model model,
      RedirectAttributes redirectAttributes) {
    Optional<Product> product = productService.findById(id);
    if (product.isEmpty()) {
      redirectAttributes.addFlashAttribute("errorMessage", "Product not found with id: " + id);
      return "redirect:/admin/products/list";
    }

    model.addAttribute("product", ProductDto.fromEntity(product.get()));
    model.addAttribute("allCategories", categoryService.findAllSortedByName());
    return "admin/product-form";
  }

  @Operation(
      summary = "Save product",
      description = "Creates a new product or updates an existing one.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Product saved, redirects to list."),
      @ApiResponse(
          responseCode = "200",
          description = "Failure: Validation errors, returns form view."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Product ID not found (for update), redirects to list.")
  })
  @PostMapping("/save")
  public String saveProduct(
      @Valid @ModelAttribute("product") ProductDto productDto,
      BindingResult bindingResult,
      Model model, RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      log.warn("Admin product form has validation errors.");
      model.addAttribute("allCategories", categoryService.findAllSortedByName());
      return "admin/product-form";
    }
    try {
      productService.save(productDto);
      log.info("Admin saved product with id: {}", productDto.getId());
      redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully.");
    } catch (ResourceNotFoundException e) {
      log.warn("Attempted to save a non-existent product with id: {}", productDto.getId(), e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/admin/products/list";
  }

  @Operation(
      summary = "List products",
      description = "Displays a list of  with optional filtering.")
  @ApiResponse(responseCode = "200", description = "List displayed successfully.")
  @GetMapping("/list")
  public String listProducts(
      @Parameter(description = "Search keyword for product name.")
      @RequestParam(value = "keyword", required = false) String keyword,

      @Parameter(description = "Filter by category IDs.")
      @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,

      @Parameter(description = "Filter by status (active/archived).")
      @RequestParam(value = "status", defaultValue = "all") String status,

      Model model) {

    List<ProductAdminView> products;
    boolean isSearch = keyword != null || categoryIds != null;

    if (isSearch || !"all".equals(status)) {
      products = productService.searchForAdminList(keyword, categoryIds, status);
    } else {
      products = productService.findAllForAdminList();
    }

    model.addAttribute("products", products);
    model.addAttribute("allCategories", categoryService.findAllSortedByName());
    model.addAttribute("keyword", keyword);
    model.addAttribute("selectedCategoryIds",
        categoryIds != null ? categoryIds : Collections.emptyList());
    model.addAttribute("status", status);
    return "admin/products-list";
  }

  @Operation(summary = "Delete product", description = "Soft-deletes a product by ID.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Product deleted, redirects to referer page."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Product not found or error, redirects to referer page.")
  })
  @DeleteMapping("/delete/{id}")
  public String deleteProducts(
      @Parameter(description = "ID of the product to delete.")
      @PathVariable Long id,

      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    try {
      productService.deleteById(id);
      log.info("Admin deleted product with id: {}", id);
      redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
    } catch (ResourceNotFoundException e) {
      log.warn("Attempted to delete a non-existent product with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error deleting product with id: {}", id, e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An error occurred while deleting the product.");
    }
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/admin/products/list");
  }

  @Operation(summary = "Restore product", description = "Restores a soft-deleted product.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Product restored, redirects to referer page."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Product not found or error, redirects to referer page.")
  })
  @PostMapping("/restore/{id}")
  public String restoreProduct(
      @Parameter(description = "ID of the product to restore.")
      @PathVariable Long id,

      RedirectAttributes redirectAttributes,
      HttpServletRequest request) {
    try {
      productService.restoreById(id);
      log.info("Admin restored product with id: {}", id);
      redirectAttributes.addFlashAttribute("successMessage", "Product restored successfully.");
    } catch (ResourceNotFoundException e) {
      log.warn("Attempted to restore a non-existent product with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Error restoring product with id: {}", id, e);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "An error occurred while restoring the product.");
    }
    String referer = request.getHeader("Referer");
    return "redirect:" + (referer != null ? referer : "/admin/products/list");
  }
}
