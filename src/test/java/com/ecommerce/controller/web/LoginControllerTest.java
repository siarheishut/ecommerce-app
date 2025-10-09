package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(LoginController.class)
@WithAnonymousUser
@SuppressWarnings("unused")
public class LoginControllerTest {

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

  @Test
  void whenShowLoginForm_withoutParams_returnsLoginForm() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/login-form"))
        .andExpect(model().attributeDoesNotExist("loginError", "redirectUrl"));
  }

  @Test
  void whenShowLoginForm_withErrorParam_returnsLoginFormWithError() throws Exception {
    mockMvc.perform(get("/login").param("error", "true"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/login-form"))
        .andExpect(model().attribute("loginError", "Invalid username or password."));
  }

  @Test
  void whenShowLoginForm_withRedirectUrl_returnsLoginFormWithRedirectUrl() throws Exception {
    mockMvc.perform(get("/login").param("redirectUrl", "/my-account"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/login-form"))
        .andExpect(model().attribute("redirectUrl", "/my-account"));
  }

  @Test
  void whenShowLoginForm_withBlankRedirectUrl_returnsLoginFormWithoutRedirectUrl() throws Exception {
    mockMvc.perform(get("/login").param("redirectUrl", "  "))
        .andExpect(status().isOk())
        .andExpect(view().name("public/login-form"))
        .andExpect(model().attributeDoesNotExist("redirectUrl"));
  }
}
