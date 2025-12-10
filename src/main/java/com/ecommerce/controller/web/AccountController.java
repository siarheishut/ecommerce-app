package com.ecommerce.controller.web;

import com.ecommerce.dto.AddressDto;
import com.ecommerce.dto.ChangePasswordDto;
import com.ecommerce.dto.PasswordResetDto;
import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.entity.User;
import com.ecommerce.exception.UserNotAuthenticatedException;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Tag(name = "Account UI", description = "User account management.")
@Controller
@RequiredArgsConstructor
public class AccountController {
  private final UserService userService;

  @Operation(
      summary = "Show change password form",
      description = "Displays the view for changing the current user's password.")
  @ApiResponse(responseCode = "200", description = "Form displayed successfully.")
  @GetMapping("/change-password")
  public String showChangePasswordForm(Model model) {
    if (!model.containsAttribute("changePasswordDto")) {
      model.addAttribute("changePasswordDto", new ChangePasswordDto());
    }
    return "public/change-password-form";
  }

  @Operation(
      summary = "Process password change",
      description = "Validates the old password and sets a new one.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Redirects to /my-account with success message."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Redirects back to form if validation fails or old password is" +
              " wrong."),
      @ApiResponse(
          responseCode = "302",
          description = "Not Authenticated: Redirects to /login if user is not authenticated.")
  })
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
        redirectAttributes.addFlashAttribute("errorMessage", "The current password is not" +
            " correct.");
        return "redirect:/change-password";
      }
    } catch (UserNotAuthenticatedException e) {
      log.warn("Anonymous user attempt to change password was blocked.", e);
      return "redirect:/login";
    }

    log.info("User successfully changed their password.");
    redirectAttributes.addFlashAttribute("successMessage",
        "Your password has been changed successfully.");
    return "redirect:/my-account";
  }

  @Operation(
      summary = "Show forgot password form",
      description = "Displays the form to request a password reset link.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Form displayed successfully."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to home page if user is already logged in.")
  })
  @GetMapping("/forgot-password")
  public String showForgotPasswordForm(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      return "redirect:/";
    }
    return "public/forgot-password-form";
  }

  @Operation(
      summary = "Request password reset",
      description = "Sends a password reset email if the account exists.")
  @ApiResponse(
      responseCode = "302",
      description = "Redirects back to form with a generic info message.")
  @PostMapping("/forgot-password")
  public String processForgotPassword(
      @Parameter(description = "User's email address")
      @RequestParam("email") String userEmail,

      RedirectAttributes redirectAttributes) {
    Optional<User> userOptional = userService.findByEmail(userEmail);
    if (userOptional.isPresent()) {
      userService.createPasswordResetTokenForUser(userOptional.get());
    } else {
      log.info("Password reset requested for non-existent user with email: {}", userEmail);
    }
    redirectAttributes.addFlashAttribute("message", "If an account with that email exists," +
        " a password reset link has been sent.");
    return "redirect:/forgot-password";
  }

  @Operation(
      summary = "Show reset password form",
      description = "Displays the form to enter a new password using a token.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Token valid, form displayed."),
      @ApiResponse(
          responseCode = "302",
          description = "Token invalid or expired: Redirects to /login with error message."),
      @ApiResponse(
          responseCode = "302",
          description = "Redirects to home page if user is already logged in.")
  })
  @GetMapping("/reset-password")
  public String showResetPasswordForm(
      @Parameter(description = "Password reset token.")
      @RequestParam("token") String token,

      Model model,
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

  @Operation(
      summary = "Process password reset",
      description = "Resets the user's password if the token is valid.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Password changed, redirects to /login."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Redirects back to form (validation error) or /login (invalid" +
              " token).")
  })
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

  @Operation(
      summary = "View my account",
      description = "Displays the dashboard with user info and addresses.")
  @ApiResponse(responseCode = "200", description = "Dashboard displayed.")
  @GetMapping("/my-account")
  public String showMyAccount(Model model) {
    User currentUser = userService.getCurrentUser();
    if (!model.containsAttribute("userInfo")) {
      model.addAttribute("userInfo", UserInfoDto.fromEntity(currentUser));
    }
    if (!model.containsAttribute("address")) {
      model.addAttribute("address", new AddressDto());
    }
    return "public/my-account";
  }
}
