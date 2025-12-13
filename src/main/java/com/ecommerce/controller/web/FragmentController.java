package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.entity.Address;
import com.ecommerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Tag(name = "UI Fragments", description = "AJAX partial views for dynamic content updates.")
@Controller
@RequestMapping("/fragments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FragmentController {
  private final AddressService addressService;

  @Operation(
      summary = "Load addresses fragment",
      description = "Fetches the HTML fragment for the address management modal.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Returns HTML fragment. <br>" +
              "• **Success:** Address modal HTML. <br>" +
              "• **Failure:** Error fragment view (if loading fails)."),
      @ApiResponse(
          responseCode = "403",
          description = "Forbidden: User is not authenticated (handled by security).")
  })
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
