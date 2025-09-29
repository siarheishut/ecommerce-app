package com.ecommerce.controller.web;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    return "/admin/product-form";
  }

  @GetMapping("/edit/{id}")
  public String showUpdateForm(@PathVariable Long id, Model model) {
    Product product = productService.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    model.addAttribute("product", ProductDto.fromEntity(product));
    model.addAttribute("allCategories", categoryService.findAllSortedByName());
    return "/admin/product-form";
  }

  @PostMapping("/save")
  public String saveProduct(
      @Valid @ModelAttribute("product") ProductDto productDto,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("allCategories", categoryService.findAllSortedByName());
      return "/admin/product-form";
    }
    productService.save(productDto);
    return "redirect:/admin/products/list";
  }

  @GetMapping("/list")
  public String listProducts(Model model) {
    model.addAttribute("products", productService.findAllForAdminList());
    return "admin/products-list";
  }

  @DeleteMapping("/delete/{id}")
  public String deleteProducts(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      productService.deleteById(id);
      redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
    } catch (Exception e) {
      log.error("Error deleting product with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the product.");
    }
    return "redirect:/admin/products/list";
  }

  @PostMapping("/restore/{id}")
  public String restoreProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      productService.restoreById(id);
      redirectAttributes.addFlashAttribute("successMessage", "Product restored successfully.");
    } catch (Exception e) {
      log.error("Error restoring product with id: {}", id, e);
      redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while restoring the product.");
    }
    return "redirect:/admin/products/list";
  }
}
