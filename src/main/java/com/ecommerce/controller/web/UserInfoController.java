package com.ecommerce.controller.web;

import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/user-info")
@RequiredArgsConstructor
public class UserInfoController {
  private final UserService userService;

  @PostMapping("/update")
  public String updateUserInfo(@Valid @ModelAttribute("userInfo") UserInfoDto userInfoDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      log.warn("User info form has validation errors for user.");
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userInfo",
          bindingResult);
      redirectAttributes.addFlashAttribute("userInfo", userInfoDto);
      redirectAttributes.addFlashAttribute("openUserInfoModal", true);
      return "redirect:/my-account";
    }

    User currentUser = userService.getCurrentUser();
    userService.updateCurrentUserInfo(userInfoDto);
    log.info("User info updated successfully for user: {}", currentUser.getUsername());
    redirectAttributes.addFlashAttribute("userInfoSuccess",
        "Your information has been updated successfully!");
    return "redirect:/my-account";
  }
}
