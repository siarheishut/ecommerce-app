package com.ecommerce.controller.api;

import com.ecommerce.dto.CartItemViewDto;
import com.ecommerce.dto.CartUpdateDto;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Cart API", description = "Operations for managing the shopping cart (via AJAX).")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

  private final CartService cartService;

  @Operation(
      summary = "Update item quantity",
      description = "Updates the quantity of a specific product in the current user's cart.")
  @ApiResponses(
      value = {
          @ApiResponse(
              responseCode = "200",
              description = "Quantity updated successfully.",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(example = "{\"status\": \"success\", \"itemTotal\": 100.0, " +
                      "\"grandTotal\": 500.0}"))),
          @ApiResponse(
              responseCode = "400",
              description = "Validation error or insufficient stock.",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(example = "{\"error\": \"Not enough stock\"}")))
      })
  @PutMapping("/update")
  public ResponseEntity<?> updateQuantity(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Cart update payload.", required = true)
      @RequestBody CartUpdateDto cartUpdateDto) {
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
  }

  @Operation(summary = "Remove item", description = "Removes a product from the cart.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Item removed successfully.",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(example = "{\"status\": \"success\", \"totalAmount\": 400.0, " +
                  "\"isEmpty\": false}"))),
      @ApiResponse(responseCode = "400", description = "Error removing item.")
  })
  @DeleteMapping("/remove")
  public ResponseEntity<?> removeItem(
      @Parameter(description = "ID of the product to remove.", required = true, example = "101")
      @RequestParam("productId") Long productId) {
    cartService.removeItem(productId);
    CartViewDto updatedCart = cartService.getCartForCurrentUser();

    return ResponseEntity.ok(Map.of(
        "status", "success",
        "totalAmount", updatedCart.totalAmount(),
        "isEmpty", updatedCart.items().isEmpty()
    ));
  }
}
