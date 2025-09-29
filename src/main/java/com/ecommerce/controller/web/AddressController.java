package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {
  private final AddressService addressService;

  @PostMapping("/save")
  public String saveAddress(@Valid @ModelAttribute("address") AddressDto addressDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
    if (addressService.isNameTakenByUser(addressDto.getName(), addressDto.getId())) {
      bindingResult.rejectValue("name", "address.name", "Address name is already in use.");
    }

    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("address", addressDto);
      redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "address", bindingResult);
      redirectAttributes.addFlashAttribute("openAddressModal", true);
      return "redirect:/";
    }

    addressService.saveAddress(addressDto);
    redirectAttributes.addFlashAttribute("addressSuccess", "Address saved successfully!");
    return "redirect:/";
  }

  @PostMapping("/delete/{id}")
  public String deleteAddress(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    addressService.deleteAddress(id);
    redirectAttributes.addFlashAttribute("addressSuccess", "Address deleted successfully!");
    return "redirect:/";
  }
}
