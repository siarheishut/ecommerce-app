package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(CustomErrorController.class)
@SuppressWarnings("unused")
public class CustomErrorControllerTest {

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
  void handleError_when404_returnsErrorPageWith404Info() throws Exception {
    mockMvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.NOT_FOUND.value())
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/non-existent-page"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/error"))
        .andExpect(model().attribute("statusCode", "404"))
        .andExpect(model().attribute(
            "errorMessage", "The page you are looking for could not be found."));
  }

  @Test
  void handleError_when403_returnsErrorPageWith403Info() throws Exception {
    mockMvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.FORBIDDEN.value())
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/admin/secret"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/error"))
        .andExpect(model().attribute("statusCode", "403"))
        .andExpect(model().attribute(
            "errorMessage", "You are not authorized to access this page."));
  }

  @Test
  void handleError_when500_returnsErrorPageWith500Info() throws Exception {
    mockMvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE,
                HttpStatus.INTERNAL_SERVER_ERROR.value())
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/some-action")
            .requestAttr(RequestDispatcher.ERROR_EXCEPTION, new RuntimeException("Some exception")))
        .andExpect(status().isOk())
        .andExpect(view().name("public/error"))
        .andExpect(model().attribute("statusCode", "500"))
        .andExpect(model().attribute(
            "errorMessage", "An internal server error occurred. Please try again later."));
  }

  @Test
  void handleError_whenNoStatus_returnsGenericErrorPage() throws Exception {
    mockMvc.perform(get("/error"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/error"))
        .andExpect(model().attribute("statusCode", "N/A"))
        .andExpect(model().attribute("errorMessage", "An unexpected error occurred"));
  }

  @Test
  void handleError_whenInvalidStatus_returnsGenericErrorPage() throws Exception {
    mockMvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, "invalid-status-code"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/error"))
        .andExpect(model().attribute("statusCode", "N/A"))
        .andExpect(model().attribute("errorMessage", "An unexpected error occurred"));
  }
}
