package com.ecommerce.controller.web;

import com.ecommerce.dto.CartUpdateDto;
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
    cartService.addProductToCart(productId, quantity);
    if (!redirectAttributes.containsAttribute("errorMessage")) {
      redirectAttributes.addFlashAttribute("successMessage",
          (quantity > 1 ? "Products have" : "Product has") + " been added to your cart");
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
      redirectAttributes.addFlashAttribute("errorMessage",
          errorMessage);
      return "redirect:/cart";
    }
    log.info("Updating product {} quantity to {}.",
        cartUpdateDto.getProductId(), cartUpdateDto.getQuantity());
    cartService.updateProductQuantity(cartUpdateDto.getProductId(), cartUpdateDto.getQuantity());
    return "redirect:/cart";
  }

  @DeleteMapping("/remove")
  public String removeItem(@RequestParam("productId") Long productId) {
    cartService.removeItem(productId);
    log.info("Removed product {} from cart.", productId);
    return "redirect:/cart";
  }

  private boolean isUrlLocal(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }
    return url.startsWith("/") && !url.startsWith("//") && !url.contains(":");
  }
}
