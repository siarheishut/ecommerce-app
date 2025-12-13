package com.ecommerce.controller.web;

import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Tag(name = "Registration", description = "User registration operations.")
@Controller
@RequiredArgsConstructor
public class RegistrationController {
  private final UserService userService;

  @Operation(
      summary = "Show registration form",
      description = "Displays the sign-up page for new users.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Registration form displayed."),
      @ApiResponse(responseCode = "302", description = "Redirects to home if already logged in.")
  })
  @GetMapping("/register")
  public String showRegistrationForm(Model model, Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }

    if (!model.containsAttribute("user")) {
      model.addAttribute("user", new RegistrationDto());
    }
    return "public/registration-form";
  }

  @Operation(summary = "Process registration", description = "Registers a new user account.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Failure: Returns form view with validation error messages."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects based on status. <br>" +
              "• **Success:** Account created, redirects to login page. <br>" +
              "• **Already Logged In:** Redirects to home.")
  })
  @PostMapping("/processRegistration")
  public String processRegistration(@Valid @ModelAttribute("user") RegistrationDto registrationDto,
                                    BindingResult bindingResult,
                                    Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }

    if (userService.existsByUsername(registrationDto.getUsername())) {
      log.warn("Registration attempt with existing username: {}", registrationDto.getUsername());
      bindingResult.rejectValue("username", "user.username",
          "An account with this username already exists.");
    }
    if (userService.existsByEmail(registrationDto.getEmail())) {
      log.warn("Registration attempt with existing email: {}", registrationDto.getEmail());
      bindingResult.rejectValue("email", "user.email",
          "An account with this email already exists.");
    }

    if (bindingResult.hasErrors()) {
      log.warn("Registration form has validation errors.");
      return "public/registration-form";
    }

    userService.registerUser(registrationDto);
    log.info("New user registered successfully: {}", registrationDto.getUsername());
    return "redirect:/login?reg_success";
  }
}
