package com.ecommerce.controller.web;

import com.ecommerce.dto.ProductAdminView;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
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

  @GetMapping("/add")
  public String showAddForm(Model model) {
    model.addAttribute("product", new ProductDto());
    model.addAttribute("allCategories", categoryService.findAllSortedByName());
    return "admin/product-form";
  }

  @GetMapping("/edit/{id}")
  public String showUpdateForm(@PathVariable Long id, Model model,
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

  @GetMapping("/list")
  public String listProducts(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
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

  @DeleteMapping("/delete/{id}")
  public String deleteProducts(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
    return "redirect:/admin/products/list";
  }

  @PostMapping("/restore/{id}")
  public String restoreProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
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
    return "redirect:/admin/products/list";
  }
}
