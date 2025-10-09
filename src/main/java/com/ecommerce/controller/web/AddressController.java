package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.exception.AddressLimitExceededException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
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
      log.warn("User tried to save an address with a duplicate name: {}", addressDto.getName());
      bindingResult.rejectValue("name", "address.name", "Address name is already in use.");
    }

    if (bindingResult.hasErrors()) {
      log.warn("Address form has validation errors for user.");
      redirectAttributes.addFlashAttribute("address", addressDto);
      redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "address",
          bindingResult);
      redirectAttributes.addFlashAttribute("openAddressModal", true);
      return "redirect:/";
    }

    try {
      addressService.saveAddress(addressDto);
      log.info("Address '{}' saved successfully for user.", addressDto.getName());
      redirectAttributes.addFlashAttribute("addressSuccess", "Address saved successfully!");
    } catch (AddressLimitExceededException e) {
      log.warn("User tried to save an address but exceeded the limit.", e);
      redirectAttributes.addFlashAttribute("addressError", e.getMessage());
    } catch (ResourceNotFoundException e) {
      log.warn("User tried to update a non-existent address with id: {}", addressDto.getId(), e);
      redirectAttributes.addFlashAttribute("addressError", e.getMessage());
    }
    return "redirect:/";
  }

  @PostMapping("/delete/{id}")
  public String deleteAddress(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
    try {
      addressService.deleteAddress(id);
      log.info("Address with id {} deleted successfully for user.", id);
      redirectAttributes.addFlashAttribute("addressSuccess", "Address deleted successfully!");
    } catch (ResourceNotFoundException e) {
      log.warn("User tried to delete a non-existent address with id: {}", id, e);
      redirectAttributes.addFlashAttribute("addressError", e.getMessage());
    }
    return "redirect:/";
  }
}
