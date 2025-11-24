package com.ecommerce.controller.api;

import com.ecommerce.dto.CartItemViewDto;
import com.ecommerce.dto.CartUpdateDto;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

  private final CartService cartService;

  @PutMapping("/update")
  public ResponseEntity<?> updateQuantity(@RequestBody CartUpdateDto cartUpdateDto) {
    try {
      cartService.updateProductQuantity(cartUpdateDto.getProductId(), cartUpdateDto.getQuantity());

      CartViewDto updatedCart = cartService.getCartForCurrentUser();

      CartItemViewDto updatedItem = updatedCart.items().stream()
          .filter(item -> item.product().id().equals(cartUpdateDto.getProductId()))
          .findFirst()
          .orElse(null);

      if (updatedItem == null) {
        return ResponseEntity.ok(Map.of(
            "removed", true,
            "totalAmount", updatedCart.totalAmount(),
            "isEmpty", updatedCart.items().isEmpty()
        ));
      }

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "itemTotal", updatedItem.product().inCartQuantity() *
              updatedItem.product().price().doubleValue(),
          "grandTotal", updatedCart.totalAmount()
      ));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/remove")
  public ResponseEntity<?> removeItem(@RequestParam("productId") Long productId) {
    try {
      cartService.removeItem(productId);
      CartViewDto updatedCart = cartService.getCartForCurrentUser();

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "totalAmount", updatedCart.totalAmount(),
          "isEmpty", updatedCart.items().isEmpty()
      ));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
