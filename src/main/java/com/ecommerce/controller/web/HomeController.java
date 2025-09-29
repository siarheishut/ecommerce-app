package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {
  private final UserService userService;

  @GetMapping("/")
  public String home() {
    return "public/index";
  }

  @ModelAttribute
  public void addAttributes(Model model, Authentication authentication) {
    if (!model.containsAttribute("address")) {
      model.addAttribute("address", new AddressDto());
    }

    if (!model.containsAttribute("userInfo")) {
      if (authentication != null && authentication.isAuthenticated()) {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
          model.addAttribute("userInfo", UserInfoDto.fromEntity(currentUser));
        } else {
          model.addAttribute("userInfo", new UserInfoDto());
        }
      } else {
        model.addAttribute("userInfo", new UserInfoDto());
      }
    }
  }
}
