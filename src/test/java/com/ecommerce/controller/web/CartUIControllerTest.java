package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(CartUIController.class)
@SuppressWarnings("unused")
public class CartUIControllerTest {

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
  private CartService cartService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  @Test
  @WithMockUser
  void viewCart_shouldReturnCartView() throws Exception {
    CartViewDto cartViewDto = new CartViewDto(Collections.emptyList(), BigDecimal.ZERO);
    when(cartService.getCartForCurrentUser()).thenReturn(cartViewDto);

    mockMvc.perform(get("/cart"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/cart"))
        .andExpect(model().attribute("cart", cartViewDto));
  }

  @Test
  @WithMockUser
  void addToCart_shouldRedirectWithSuccessMessage() throws Exception {
    mockMvc.perform(post("/cart/add")
            .param("productId", "1")
            .param("quantity", "2")
            .param("returnUrl", "/some-page")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/some-page"))
        .andExpect(flash().attribute("successMessage", "Products have been added to your cart"));

    verify(cartService).addProductToCart(1L, 2);
  }

  @Test
  @WithMockUser
  void updateQuantity_withValidData_shouldRedirectToCart() throws Exception {
    mockMvc.perform(put("/cart/update")
            .param("productId", "1")
            .param("quantity", "5")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"));

    verify(cartService).updateProductQuantity(1L, 5);
  }

  @Test
  @WithMockUser
  void updateQuantity_withInvalidData_shouldRedirectWithErrorMessage() throws Exception {
    mockMvc.perform(put("/cart/update")
            .param("productId", "1")
            .param("quantity", "0")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attribute("errorMessage", "Quantity must be at least 1."));
  }

  @Test
  @WithMockUser
  void removeItem_shouldRedirectToCart() throws Exception {
    mockMvc.perform(delete("/cart/remove")
            .param("productId", "1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"));

    verify(cartService).removeItem(1L);
  }
}
