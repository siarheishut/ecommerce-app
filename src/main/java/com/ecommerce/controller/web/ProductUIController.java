package com.ecommerce.controller.web;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewSubmissionDto;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
@Validated
@Slf4j
public class ProductUIController {
  private final ProductService productService;
  private final CategoryService categoryService;
  private final ReviewService reviewService;

  @GetMapping("/list")
  public String showProductList(
      @RequestParam(required = false)
      String name,
      @RequestParam(required = false)
      List<Long> categoryIds,
      @RequestParam(required = false)
      @DecimalMin(value = "0.00", message = "Min price must be greater than 0")
      Double minPrice,
      @RequestParam(required = false)
      @DecimalMax(value = "999999.99", message = "Max price must be less than 1'000'000")
      Double maxPrice,
      @RequestParam(required = false)
      Boolean onlyAvailable,
      @RequestParam(defaultValue = "0")
      @Min(value = 0, message = "Page number must be greater than or equal to 0")
      int page,
      @RequestParam(defaultValue = "12")
      @Min(value = 1, message = "Page size must be greater than 0")
      @Max(value = 100, message = "Page size must be less than or equal to 100")
      int size,
      Model model,
      HttpServletRequest request) {
    log.info("Searching for products with parameters - name: {}, categoryIds: {}, minPrice: {}, " +
            "maxPrice: {}, onlyAvailable: {}, page: {}, size: {}",
        name, categoryIds != null ?
            categoryIds.stream().map(String::valueOf).collect(Collectors.joining(",")) : "null",
        minPrice, maxPrice, onlyAvailable, page, size);

    Pageable pageable = PageRequest.of(page, size);
    Page<Product> productPage = productService.searchProducts(
        name, categoryIds, minPrice, maxPrice, onlyAvailable, pageable);

    List<Category> categories = categoryService.findAllSortedByName();

    model.addAttribute("productPage", productPage);
    model.addAttribute("categories", categories);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("selectedCategoryIds",
        categoryIds != null ? categoryIds : java.util.Collections.emptyList());
    model.addAttribute("searchName", name);
    model.addAttribute("onlyAvailable", onlyAvailable != null && onlyAvailable);

    String requestUri = request.getRequestURI();
    String queryString = request.getQueryString();
    String returnUrl = requestUri + (queryString != null ? "?" + queryString : "");
    model.addAttribute("returnUrl", returnUrl);
    return "public/products-list";
  }

  @GetMapping("/{id}")
  public String productDetail(@PathVariable("id") Long id, Model model,
                              @PageableDefault(size = 5) Pageable pageable,
                              RedirectAttributes redirectAttributes) {
    log.info("Requesting product detail page for product ID: {}", id);
    try {
      Product product = productService.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found."));
      Page<ReviewDto> reviewsPage = reviewService.getReviewsForProduct(id, pageable);
      model.addAttribute("product", product);
      model.addAttribute("reviewsPage", reviewsPage);
      if (!model.containsAttribute("newReview")) {
        model.addAttribute("newReview", new ReviewSubmissionDto(null, ""));
      }
    } catch (ResourceNotFoundException e) {
      log.warn("Product with ID {} not found.", id);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/products/list";
    }

    return "public/product-detail";
  }
}
