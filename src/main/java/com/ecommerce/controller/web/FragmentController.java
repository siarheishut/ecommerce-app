package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.entity.Address;
import com.ecommerce.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/fragments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FragmentController {
  private final AddressService addressService;

  @GetMapping("/addresses")
  public String getAddressesFragment(Model model) {
    try {
      List<Address> addresses = addressService.getAddressesForCurrentUser();
      model.addAttribute("addresses", addresses);
      if (!model.containsAttribute("address")) {
        model.addAttribute("address", new AddressDto());
      }
      log.info("Successfully loaded address fragment for current user.");
      return "fragments/address-modal :: address-modals-content";
    } catch (Exception e) {
      log.error("Error loading addresses fragment for current user", e);
      return "fragments/error-fragment :: content";
    }
  }
}
