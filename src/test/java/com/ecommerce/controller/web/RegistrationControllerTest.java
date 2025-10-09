package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(RegistrationController.class)
@WithAnonymousUser
@SuppressWarnings("unused")
public class RegistrationControllerTest {

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
  private StringToCategoryConverter stringToCategoryConverter;

  @MockitoBean
  private UserService userService;

  @Test
  void whenShowRegistrationForm_returnsFormViewWithUserAttribute() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/registration-form"))
        .andExpect(model().attributeExists("user"));
  }

  @Test
  void whenProcessRegistration_withValidData_registersUserAndRedirectsToLogin() throws Exception {
    when(userService.existsByUsername(anyString())).thenReturn(false);
    when(userService.existsByEmail(anyString())).thenReturn(false);

    ArgumentCaptor<RegistrationDto> captor = ArgumentCaptor.forClass(RegistrationDto.class);

    mockMvc.perform(post("/processRegistration")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "newuser")
            .param("email", "new@test.com")
            .param("password", "password123")
            .param("confirmPassword", "password123")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?reg_success"));

    verify(userService).registerUser(captor.capture());
    assertThat(captor.getValue().getUsername()).isEqualTo("newuser");
    assertThat(captor.getValue().getEmail()).isEqualTo("new@test.com");
  }

  @Test
  void whenProcessRegistration_withExistingUsername_returnsFormWithErrors() throws Exception {
    when(userService.existsByUsername("existinguser")).thenReturn(true);

    mockMvc.perform(post("/processRegistration")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "existinguser")
            .param("email", "new@test.com")
            .param("password", "password123")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("public/registration-form"))
        .andExpect(model().attributeHasFieldErrors("user", "username"));

    verify(userService, never()).registerUser(any());
  }

  @Test
  void whenProcessRegistration_withExistingEmail_returnsFormWithErrors() throws Exception {
    when(userService.existsByUsername(anyString())).thenReturn(false);
    when(userService.existsByEmail("existing@test.com")).thenReturn(true);

    mockMvc.perform(post("/processRegistration")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "newuser")
            .param("email", "existing@test.com")
            .param("password", "password123")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("public/registration-form"))
        .andExpect(model().attributeHasFieldErrors("user", "email"));

    verify(userService, never()).registerUser(any());
  }
}
