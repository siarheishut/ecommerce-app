package com.ecommerce.controller.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

  @GetMapping("/login")
  public String showLoginForm(
      @RequestParam(name = "redirectUrl", required = false) String redirectUrl,
      @RequestParam(name = "error", required = false) String error,
      Model model, Authentication authentication) {
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
