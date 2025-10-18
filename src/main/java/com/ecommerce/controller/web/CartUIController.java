package com.ecommerce.controller.web;

import com.ecommerce.dto.CartUpdateDto;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartUIController {
  private final CartService cartService;

  @GetMapping
  public String viewCart(Model model) {
    log.info("User viewing cart.");
    model.addAttribute("cart", cartService.getCartForCurrentUser());
    return "public/cart";
  }

  @PostMapping("/add")
  public String addToCart(@RequestParam("productId") Long productId,
                          @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                          @RequestParam(name = "returnUrl", defaultValue = "/products/list")
                          String returnUrl,
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
      return "redirect:" + returnUrl;
    } else {
      log.warn("Attempted redirect to a non-local URL was blocked: {}", returnUrl);
      return "redirect:/products/list";
    }
  }

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

  @DeleteMapping("/remove")
  public String removeItem(@RequestParam("productId") Long productId,
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
