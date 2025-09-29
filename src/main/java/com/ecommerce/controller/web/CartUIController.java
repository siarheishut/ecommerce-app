package com.ecommerce.controller.web;

import com.ecommerce.dto.CartUpdateDto;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartUIController {
  private final CartService cartService;

  @GetMapping
  public String viewCart(Model model) {
    model.addAttribute("cart", cartService.getCartForCurrentUser());
    return "public/cart";
  }

  @PostMapping("/add")
  public String addToCart(@RequestParam("productId") Long productId,
                          @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                          @RequestParam(name = "returnUrl", defaultValue = "/products/list")
                          String returnUrl,
                          RedirectAttributes redirectAttributes) {
    cartService.addProductToCart(productId, quantity);
    if (!redirectAttributes.containsAttribute("errorMessage")) {
      redirectAttributes.addFlashAttribute("successMessage",
          (quantity > 1 ? "Products have" : "Product has") + " been added to your cart");
    }
    return "redirect:" + returnUrl;
  }

  @PutMapping("/update")
  public String updateQuantity(@Valid @ModelAttribute CartUpdateDto cartUpdateDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("errorMessage",
          bindingResult.getAllErrors().getFirst().getDefaultMessage());
      return "redirect:/cart";
    }
    cartService.updateProductQuantity(cartUpdateDto.getProductId(), cartUpdateDto.getQuantity());
    return "redirect:/cart";
  }

  @DeleteMapping("/remove")
  public String removeItem(@RequestParam("productId") Long productId) {
    cartService.removeItem(productId);
    return "redirect:/cart";
  }
}
