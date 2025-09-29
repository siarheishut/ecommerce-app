package com.ecommerce.controller.web;

import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user-info")
@RequiredArgsConstructor
public class UserInfoController {
  private final UserService userService;

  @PostMapping("/update")
  public String updateUserInfo(@Valid UserInfoDto userInfoDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userInfo", bindingResult);
      redirectAttributes.addFlashAttribute("userInfo", userInfoDto);
      redirectAttributes.addFlashAttribute("openUserInfoModal", true);
      return "redirect:/";
    }

    userService.updateCurrentUserInfo(userInfoDto);
    redirectAttributes.addFlashAttribute("userInfoSuccess", "Your information has been updated successfully!");
    return "redirect:/";
  }
}
