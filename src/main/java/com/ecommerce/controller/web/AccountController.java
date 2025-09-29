package com.ecommerce.controller.web;

import com.ecommerce.dto.ChangePasswordDto;
import com.ecommerce.dto.PasswordResetDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            redirectAttributes.addFlashAttribute("changePasswordDto", changePasswordDto);
            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "changePasswordDto", bindingResult);
            return "redirect:/change-password";
        }

        if (!userService.changeCurrentUserPassword(changePasswordDto)) {
            redirectAttributes.addFlashAttribute("changePasswordDto", changePasswordDto);
            redirectAttributes.addFlashAttribute("errorMessage", "The current password is not correct.");
            return "redirect:/change-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Your password has been changed successfully.");
        return "redirect:/";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "public/forgot-password-form";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String userEmail,
                                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            userService.createPasswordResetTokenForUser(user);
        } catch (UsernameNotFoundException _) {
        }
        redirectAttributes.addFlashAttribute("message", "If an account with that email exists, " +
                "a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model,
                                        RedirectAttributes redirectAttributes) {
        if (!userService.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired password reset token.");
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
            redirectAttributes.addFlashAttribute("message", "You have successfully changed your password.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reset-password?token=" + passwordResetDto.getToken();
        }
    }
}
