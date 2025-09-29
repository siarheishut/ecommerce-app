package com.ecommerce.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginController {

  @GetMapping("/login")
  public String showLoginForm(@RequestParam(name = "redirectUrl", required = false) String redirectUrl,
                              @RequestParam(name = "error", required = false) String error,
                              Model model) {
    if (redirectUrl != null && !redirectUrl.isBlank()) {
      model.addAttribute("redirectUrl", redirectUrl);
    }
    if (error != null) {
      model.addAttribute("loginError", "Invalid username or password.");
    }
    return "public/login-form";
  }
}
