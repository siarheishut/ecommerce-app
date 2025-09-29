package com.ecommerce.controller.web;

import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.ShippingDetailsDto;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.service.AddressService;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

  @GetMapping("/confirmation")
  public String orderConfirmation() {
    return "public/order-confirmation";
  }

  @GetMapping("/shipping-details")
  public String shippingDetailsForm(Model model) {
    if (cartService.getCartForCurrentUser().items().isEmpty()) {
      return "redirect:/cart";
    }
    setUserInfo(model);
    List<Address> addresses = addressService.getAddressesForCurrentUser();
    model.addAttribute("addresses", addresses);
    return "public/shipping-details";
  }

  @PostMapping("/place-order")
  public String placeOrder(@Valid @ModelAttribute("shippingDetails")
                           ShippingDetailsDto shippingDetailsDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("shippingDetails", shippingDetailsDto);
      redirectAttributes.addFlashAttribute(
              BindingResult.MODEL_KEY_PREFIX + "shippingDetails", bindingResult);
      return "redirect:/orders/shipping-details";
    }
    orderService.placeOrder(shippingDetailsDto);
    return "redirect:/orders/confirmation";
  }

  @GetMapping("/history")
  public String orderHistory(Model model) {
    List<OrderHistoryDto> orderHistory = orderService.getOrderHistoryForCurrentUser();
    model.addAttribute("orders", orderHistory);
    return "public/order-history";
  }
}
