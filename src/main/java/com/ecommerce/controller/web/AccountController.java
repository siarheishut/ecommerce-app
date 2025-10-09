package com.ecommerce.controller.web;

import com.ecommerce.dto.ChangePasswordDto;
import com.ecommerce.dto.PasswordResetDto;
import com.ecommerce.entity.User;
import com.ecommerce.exception.UserNotAuthenticatedException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {
  private final UserService userService;

  @GetMapping("/change-password")
  public String showChangePasswordForm(Model model) {
    if (!model.containsAttribute("changePasswordDto")) {
      model.addAttribute("changePasswordDto", new ChangePasswordDto());
    }
    return "public/change-password-form";
  }

  @PostMapping("/change-password")
  public String processChangePassword(
      @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      log.warn("Change password form has validation errors.");
      redirectAttributes.addFlashAttribute("changePasswordDto", changePasswordDto);
      redirectAttributes.addFlashAttribute(
          BindingResult.MODEL_KEY_PREFIX + "changePasswordDto", bindingResult);
      return "redirect:/change-password";
    }

    try {
      if (!userService.changeCurrentUserPassword(changePasswordDto)) {
        log.warn("User failed to change password due to incorrect current password.");
        redirectAttributes.addFlashAttribute("changePasswordDto", changePasswordDto);
        redirectAttributes.addFlashAttribute("errorMessage", "The current password is not correct.");
        return "redirect:/change-password";
      }
    } catch (UserNotAuthenticatedException e) {
      log.warn("Anonymous user attempt to change password was blocked.", e);
      return "redirect:/login";
    }

    log.info("User successfully changed their password.");
    redirectAttributes.addFlashAttribute("successMessage",
        "Your password has been changed successfully.");
    return "redirect:/";
  }

  @GetMapping("/forgot-password")
  public String showForgotPasswordForm(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }
    return "public/forgot-password-form";
  }

  @PostMapping("/forgot-password")
  public String processForgotPassword(@RequestParam("email") String userEmail,
                                      RedirectAttributes redirectAttributes) {
    Optional<User> userOptional = userService.findByEmail(userEmail);
    if (userOptional.isPresent()) {
      userService.createPasswordResetTokenForUser(userOptional.get());
    } else {
      log.info("Password reset requested for non-existent user with email: {}", userEmail);
    }
    redirectAttributes.addFlashAttribute("message", "If an account with that email exists, " +
        "a password reset link has been sent.");
    return "redirect:/forgot-password";
  }

  @GetMapping("/reset-password")
  public String showResetPasswordForm(@RequestParam("token") String token, Model model,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }

    if (!userService.validatePasswordResetToken(token)) {
      log.warn("Attempt to use invalid or expired password reset token: {}", token);
      redirectAttributes.addFlashAttribute(
          "errorMessage", "Invalid or expired password reset token.");
      return "redirect:/login";
    }
    PasswordResetDto dto = new PasswordResetDto();
    dto.setToken(token);
    model.addAttribute("passwordResetDto", dto);
    return "public/reset-password-form";
  }

  @PostMapping("/reset-password")
  public String processResetPassword(
      @Valid @ModelAttribute("passwordResetDto") PasswordResetDto passwordResetDto,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      log.warn("Reset password form has validation errors for token: {}",
          passwordResetDto.getToken());
      redirectAttributes.addFlashAttribute("passwordResetDto", passwordResetDto);
      redirectAttributes.addFlashAttribute(
          BindingResult.MODEL_KEY_PREFIX + "passwordResetDto", bindingResult);
      return "redirect:/reset-password?token=" + passwordResetDto.getToken();
    }
    try {
      User user = userService.findByPasswordResetToken(passwordResetDto.getToken())
          .orElseThrow(() -> new IllegalArgumentException(
              "Invalid or expired password reset token. Please request a new one."));
      userService.changeUserPassword(user, passwordResetDto.getPassword());
      log.info("Password successfully reset for user: {}", user.getUsername());
      redirectAttributes.addFlashAttribute(
          "message", "You have successfully changed your password.");
      return "redirect:/login";
    } catch (IllegalArgumentException e) {
      log.warn("Password reset attempt with invalid token: {}", passwordResetDto.getToken(), e);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
      return "redirect:/reset-password?token=" + passwordResetDto.getToken();
    }
  }
}
