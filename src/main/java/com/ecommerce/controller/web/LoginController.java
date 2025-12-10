package com.ecommerce.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Tag(name = "Authentication", description = "User login and authentication operations.")
@Controller
@RequiredArgsConstructor
public class LoginController {

  @Operation(
      summary = "Show login form",
      description = "Displays the login page or redirects to home if already authenticated.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Login form displayed successfully."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to home page if user is already logged in.")
  })
  @GetMapping("/login")
  public String showLoginForm(
      @Parameter(description = "URL to redirect to after successful login.")
      @RequestParam(name = "redirectUrl", required = false) String redirectUrl,

      @Parameter(description = "Error flag indicating invalid credentials.")
      @RequestParam(name = "error", required = false) String error,

      Model model,
      Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }

    if (isUrlLocal(redirectUrl)) {
      model.addAttribute("redirectUrl", redirectUrl);
    } else if (redirectUrl != null) {
      log.warn("Attempted redirect to a non-local URL was blocked: {}", redirectUrl);
    }
    if (error != null) {
      model.addAttribute("loginError", "Invalid username or password.");
      log.warn("Login attempt with invalid credentials.");
    }
    return "public/login-form";
  }

  private boolean isUrlLocal(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }
    return url.startsWith("/") && !url.startsWith("//") && !url.contains(":");
  }
}
