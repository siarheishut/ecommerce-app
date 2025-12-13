package com.ecommerce.controller.web;

import com.ecommerce.dto.CartUpdateDto;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Tag(
    name = "Shopping Cart UI",
    description = "Operations for viewing and managing the shopping cart.")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartUIController {
  private final CartService cartService;

  @Operation(
      summary = "View cart",
      description = "Displays the shopping cart page with current items and total.")
  @ApiResponse(responseCode = "200", description = "Cart page displayed successfully.")
  @GetMapping
  public String viewCart(Model model) {
    log.info("User viewing cart.");
    model.addAttribute("cart", cartService.getCartForCurrentUser());
    return "public/cart";
  }

  @Operation(
      summary = "Add to cart",
      description = "Adds a product to the cart and redirects back to the browsing page.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to return URL (or product page). <br>" +
              "• **Success:** Product added to cart. <br>" +
              "• **Failure:** Insufficient stock or invalid product (redirects with error).")
  })
  @PostMapping("/add")
  public String addToCart(
      @Parameter(description = "ID of the product to add.")
      @RequestParam("productId") Long productId,

      @Parameter(description = "Quantity to add.")
      @RequestParam(value = "quantity", defaultValue = "1") int quantity,

      @Parameter(description = "URL to redirect back to.")
      @RequestParam(name = "returnUrl", defaultValue = "/products/list") String returnUrl,

      RedirectAttributes redirectAttributes) {
    log.info("Adding product {} to cart with quantity {}.", productId, quantity);
    try {
      cartService.addProductToCart(productId, quantity);
      redirectAttributes.addFlashAttribute("successMessage",
          (quantity > 1 ? "Products have" : "Product has") + " been added to your cart");
    } catch (InsufficientStockException | ResourceNotFoundException e) {
      log.warn("Failed to add product {} to cart: {}", productId, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }

    if (isUrlLocal(returnUrl)) {
      return "redirect:" + returnUrl + "#product-" + productId;
    } else {
      log.warn("Attempted redirect to a non-local URL was blocked: {}", returnUrl);
      return "redirect:/products/list#product-" + productId;
    }
  }

  @Operation(
      summary = "Update cart item",
      description = "Updates the quantity of a product via form submission.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to cart. <br>" +
              "• **Success:** Quantity updated. <br>" +
              "• **Failure:** Validation or stock error (redirects with message).")
  })
  @PutMapping("/update")
  public String updateQuantity(@Valid @ModelAttribute CartUpdateDto cartUpdateDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      String errorMessage = bindingResult.getAllErrors().getFirst().getDefaultMessage();
      log.warn("Invalid cart update request: {}", errorMessage);
      redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
      return "redirect:/cart";
    }
    log.info("Updating product {} quantity to {}.",
        cartUpdateDto.getProductId(), cartUpdateDto.getQuantity());
    try {
      cartService.updateProductQuantity(cartUpdateDto.getProductId(), cartUpdateDto.getQuantity());
      redirectAttributes.addFlashAttribute("successMessage", "Cart updated successfully.");
    } catch (InsufficientStockException | ResourceNotFoundException e) {
      log.warn("Failed to update cart for product {}: {}",
          cartUpdateDto.getProductId(), e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/cart";
  }

  @Operation(
      summary = "Remove cart item",
      description = "Removes a product from the cart via form submission.")
  @ApiResponse(responseCode = "302", description = "Success: Item removed, redirects to cart.")
  @DeleteMapping("/remove")
  public String removeItem(
      @Parameter(description = "ID of the product to remove.")
      @RequestParam("productId") Long productId,

      RedirectAttributes redirectAttributes) {
    cartService.removeItem(productId);
    log.info("Removed product {} from cart.", productId);
    redirectAttributes.addFlashAttribute("successMessage",
        "Product has been removed from your cart.");
    return "redirect:/cart";
  }

  private boolean isUrlLocal(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }
    return url.startsWith("/") && !url.startsWith("//") && !url.contains(":");
  }
}
