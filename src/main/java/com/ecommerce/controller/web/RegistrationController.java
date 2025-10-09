package com.ecommerce.controller.web;

import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.service.UserService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegistrationController {
  private final UserService userService;

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

  @PostMapping("/processRegistration")
  public String processRegistration(@Valid @ModelAttribute("user") RegistrationDto registrationDto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
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
