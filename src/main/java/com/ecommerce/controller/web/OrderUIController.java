package com.ecommerce.controller.web;

import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.ShippingDetailsDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.AddressService;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Slf4j
@Tag(name = "Order Checkout", description = "Order placement and history pages.")
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderUIController {
  private final UserService userService;
  private final AddressService addressService;
  private final CartService cartService;
  private final OrderService orderService;

  private void setUserInfo(Model model) {
    if (!model.containsAttribute("shippingDetails")) {
      ShippingDetailsDto shippingDetailsDto = new ShippingDetailsDto();
      User currentUser = userService.getCurrentUser();
      if (currentUser != null) {
        shippingDetailsDto.setFirstName(currentUser.getFirstName());
        shippingDetailsDto.setLastName(currentUser.getLastName());
        shippingDetailsDto.setEmail(currentUser.getEmail());
        shippingDetailsDto.setPhoneNumber(currentUser.getPhoneNumber());
      }
      model.addAttribute("shippingDetails", shippingDetailsDto);
    }
  }

  @Operation(
      summary = "Order confirmation page",
      description = "Displays the success page after placing an order.")
  @ApiResponse(responseCode = "200", description = "Page displayed successfully.")
  @GetMapping("/confirmation")
  public String orderConfirmation() {
    log.info("Displaying order confirmation page.");
    return "public/order-confirmation";
  }

  @Operation(
      summary = "Shipping details form",
      description = "Displays the form to enter shipping information.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Form displayed with user data if available."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to cart if it is empty.")
  })
  @GetMapping("/shipping-details")
  public String shippingDetailsForm(Model model) {
    if (cartService.getCartForCurrentUser().items().isEmpty()) {
      log.warn("User attempted to access shipping details with an empty cart. Redirecting to " +
          "cart.");
      return "redirect:/cart";
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated() &&
        !(authentication.getPrincipal() instanceof String)) {
      setUserInfo(model);
      model.addAttribute("addresses", addressService.getAddressesForCurrentUser());
    } else {
      model.addAttribute("shippingDetails", new ShippingDetailsDto());
      model.addAttribute("addresses", Collections.emptyList());
    }
    return "public/shipping-details";
  }

  @Operation(
      summary = "Place order",
      description = "Validates shipping details and creates the order.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Order placed, redirects to confirmation page."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Validation errors, redirects back to shipping details form.")
  })
  @PostMapping("/place-order")
  public String placeOrder(@Valid @ModelAttribute("shippingDetails")
                           ShippingDetailsDto shippingDetailsDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      log.warn("Shipping details validation failed. Redirecting back to form.");
      redirectAttributes.addFlashAttribute("shippingDetails", shippingDetailsDto);
      redirectAttributes.addFlashAttribute(
          BindingResult.MODEL_KEY_PREFIX + "shippingDetails", bindingResult);
      return "redirect:/orders/shipping-details";
    }
    orderService.placeOrder(shippingDetailsDto);
    log.info("Order placed successfully. Redirecting to confirmation page.");
    return "redirect:/orders/confirmation";
  }

  @Operation(
      summary = "Order history",
      description = "Displays a list of past orders for the logged-in user.")
  @ApiResponse(responseCode = "200", description = "List displayed successfully.")
  @GetMapping("/history")
  public String orderHistory(Model model) {
    List<OrderHistoryDto> orderHistory = orderService.getOrderHistoryForCurrentUser();
    model.addAttribute("orders", orderHistory);
    log.info("Displaying order history for current user. Found {} orders.", orderHistory.size());
    return "public/order-history";
  }
}
