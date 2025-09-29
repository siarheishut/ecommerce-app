package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.entity.Address;
import com.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/fragments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FragmentController {
  private final AddressService addressService;

  @GetMapping("/addresses")
  public String getAddressesFragment(Model model) {
    List<Address> addresses = addressService.getAddressesForCurrentUser();
    model.addAttribute("addresses", addresses);
    if (!model.containsAttribute("address")) {
      model.addAttribute("address", new AddressDto());
    }
    return "fragments/address-modal :: address-modals-content";
  }
}
