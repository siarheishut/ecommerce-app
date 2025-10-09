package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.entity.Address;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(FragmentController.class)
@SuppressWarnings("unused")
public class FragmentControllerTest {

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
  private AddressService addressService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  @Test
  @WithMockUser
  void getAddressesFragment_whenAuthenticated_returnsAddressFragment() throws Exception {
    when(addressService.getAddressesForCurrentUser())
        .thenReturn(Collections.singletonList(new Address()));

    mockMvc.perform(get("/fragments/addresses"))
        .andExpect(status().isOk())
        .andExpect(view().name("fragments/address-modal :: address-modals-content"))
        .andExpect(model().attributeExists("addresses", "address"));
  }

  @Test
  @WithMockUser
  void getAddressesFragment_whenServiceThrowsException_returnsErrorFragment() throws Exception {
    when(addressService.getAddressesForCurrentUser())
        .thenThrow(new RuntimeException("Some error"));

    mockMvc.perform(get("/fragments/addresses"))
        .andExpect(status().isOk())
        .andExpect(view().name("fragments/error-fragment :: content"));
  }

  @Test
  @WithAnonymousUser
  void getAddressesFragment_whenNotAuthenticated_isRedirectedToLogin() throws Exception {
    mockMvc.perform(get("/fragments/addresses"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }
}
