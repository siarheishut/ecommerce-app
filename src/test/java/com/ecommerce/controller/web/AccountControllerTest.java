package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.ChangePasswordDto;
import com.ecommerce.entity.User;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(AccountController.class)
@SuppressWarnings("unused")
public class AccountControllerTest {

  // Beans for SecurityConfig dependencies
  @MockitoBean
  private JpaUserDetailsService jpaUserDetailsService;
  @MockitoBean
  private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  @MockitoBean
  private CustomAccessDeniedHandler customAccessDeniedHandler;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  @Test
  @WithMockUser
  void whenShowChangePasswordForm_returnsForm() throws Exception {
    mockMvc.perform(get("/change-password"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/change-password-form"))
        .andExpect(model().attributeExists("changePasswordDto"));
  }

  @Test
  @WithMockUser
  void whenProcessChangePassword_withValidData_redirectsWithSuccess() throws Exception {
    when(userService.changeCurrentUserPassword(any(ChangePasswordDto.class))).thenReturn(true);

    mockMvc.perform(post("/change-password")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("currentPassword", "oldPass")
            .param("newPassword", "newPass123")
            .param("confirmPassword", "newPass123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attribute("successMessage", "Your password has been changed successfully."));
  }

  @Test
  @WithMockUser
  void whenProcessChangePassword_withInvalidData_redirectsWithBindingErrors() throws Exception {
    mockMvc.perform(post("/change-password")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("newPassword", "short"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/change-password"))
        .andExpect(flash().attributeExists("changePasswordDto", "org.springframework.validation.BindingResult.changePasswordDto"));

    verify(userService, never()).changeCurrentUserPassword(any());
  }

  @Test
  @WithMockUser
  void whenProcessChangePassword_withIncorrectCurrentPassword_redirectsWithError() throws Exception {
    when(userService.changeCurrentUserPassword(any(ChangePasswordDto.class))).thenReturn(false);

    mockMvc.perform(post("/change-password")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("currentPassword", "wrongOldPass")
            .param("newPassword", "newPass123")
            .param("confirmPassword", "newPass123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/change-password"))
        .andExpect(flash().attribute("errorMessage", "The current password is not correct."));
  }

  @Test
  @WithAnonymousUser
  void whenShowForgotPasswordForm_returnsForm() throws Exception {
    mockMvc.perform(get("/forgot-password"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/forgot-password-form"));
  }

  @Test
  @WithAnonymousUser
  void whenProcessForgotPassword_withExistingEmail_redirectsWithMessage() throws Exception {
    when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

    mockMvc.perform(post("/forgot-password")
            .with(csrf())
            .param("email", "test@example.com"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/forgot-password"))
        .andExpect(flash().attributeExists("message"));

    verify(userService).createPasswordResetTokenForUser(any(User.class));
  }

  @Test
  @WithAnonymousUser
  void whenProcessForgotPassword_withNonExistentEmail_redirectsWithMessage() throws Exception {
    when(userService.findByEmail(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(post("/forgot-password")
            .with(csrf())
            .param("email", "nouser@example.com"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/forgot-password"))
        .andExpect(flash().attributeExists("message"));

    verify(userService, never()).createPasswordResetTokenForUser(any(User.class));
  }

  @Test
  @WithAnonymousUser
  void whenShowResetPasswordForm_withValidToken_returnsForm() throws Exception {
    when(userService.validatePasswordResetToken("valid-token")).thenReturn(true);

    mockMvc.perform(get("/reset-password").param("token", "valid-token"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/reset-password-form"))
        .andExpect(model().attributeExists("passwordResetDto"));
  }

  @Test
  @WithAnonymousUser
  void whenShowResetPasswordForm_withInvalidToken_redirectsToLogin() throws Exception {
    when(userService.validatePasswordResetToken("invalid-token")).thenReturn(false);

    mockMvc.perform(get("/reset-password").param("token", "invalid-token"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"))
        .andExpect(flash().attribute("errorMessage", "Invalid or expired password reset token."));
  }

  @Test
  @WithAnonymousUser
  void whenProcessResetPassword_withValidData_redirectsToLogin() throws Exception {
    User user = new User();
    user.setUsername("testuser");
    when(userService.findByPasswordResetToken("valid-token")).thenReturn(Optional.of(user));

    mockMvc.perform(post("/reset-password")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("token", "valid-token")
            .param("password", "newPass123")
            .param("confirmPassword", "newPass123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"))
        .andExpect(flash().attribute("message", "You have successfully changed your password."));

    verify(userService).changeUserPassword(eq(user), eq("newPass123"));
  }

  @Test
  @WithAnonymousUser
  void whenProcessResetPassword_withInvalidToken_redirectsWithError() throws Exception {
    when(userService.findByPasswordResetToken("invalid-token")).thenReturn(Optional.empty());

    mockMvc.perform(post("/reset-password")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("token", "invalid-token")
            .param("password", "newPass123")
            .param("confirmPassword", "newPass123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/reset-password?token=invalid-token"))
        .andExpect(flash().attributeExists("errorMessage"));

    verify(userService, never()).changeUserPassword(any(), any());
  }
}
