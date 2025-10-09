package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(HomeController.class)
@SuppressWarnings("unused")
public class HomeControllerTest {

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
  @WithMockUser
  void whenHomePage_asUnauthenticatedUser_returnsIndexViewWithDefaultUserInfo() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/index"))
        .andExpect(model().attributeExists("address"))
        .andExpect(model().attribute("userInfo", is(new UserInfoDto())));
  }

  @Test
  @WithMockUser(username = "testuser")
  void whenHomePage_asAuthenticatedUser_returnsIndexViewWithUserInfo() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/index"))
        .andExpect(model().attributeExists("address"));
  }
}
