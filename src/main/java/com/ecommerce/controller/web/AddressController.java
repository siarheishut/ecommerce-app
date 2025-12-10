package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.exception.AddressLimitExceededException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Address Management", description = "Managing user shipping addresses.")
@Controller
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {
  private final AddressService addressService;

  @Operation(
      summary = "Save address",
      description = "Creates a new address or updates an existing one for the current user.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Address saved, redirects to /my-account."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Validation error or duplicate name, redirects to /my-account" +
              " with error details."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Address limit exceeded, redirects to /my-account with error" +
              " message.")
  })
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
      return "redirect:/my-account";
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
    return "redirect:/my-account";
  }

  @Operation(summary = "Delete address", description = "Removes an address by its ID.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Address deleted, redirects to /my-account."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Address not found, redirects to /my-account with error message.")
  })
  @PostMapping("/delete/{id}")
  public String deleteAddress(
      @Parameter(description = "ID of the address to delete.")
      @PathVariable("id") Long id,

      RedirectAttributes redirectAttributes) {
    try {
      addressService.deleteAddress(id);
      log.info("Address with id {} deleted successfully for user.", id);
      redirectAttributes.addFlashAttribute("addressSuccess", "Address deleted successfully!");
    } catch (ResourceNotFoundException e) {
      log.warn("User tried to delete a non-existent address with id: {}", id, e);
      redirectAttributes.addFlashAttribute("addressError", e.getMessage());
    }
    return "redirect:/my-account";
  }
}
