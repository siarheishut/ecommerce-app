package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;
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

  void assert_viewCart_returnCartView() throws Exception {
    CartViewDto cartViewDto = new CartViewDto(Collections.emptyList(), BigDecimal.ZERO);
    when(cartService.getCartForCurrentUser()).thenReturn(cartViewDto);

    mockMvc.perform(get("/cart"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/cart"))
        .andExpect(model().attribute("cart", cartViewDto));
  }

  @Test
  @WithAnonymousUser
  void viewCart_returnCartView_forAnonymousUser() throws Exception {
    assert_viewCart_returnCartView();
  }

  @Test
  @WithMockUser
  void viewCart_returnCartView_forMockUser() throws Exception {
    assert_viewCart_returnCartView();
  }

  void assert_addToCart_withNonexistentId_redirectWithErrorMessage() throws Exception {
    doThrow(new ResourceNotFoundException("Product with ID 1 not found."))
        .when(cartService).addProductToCart(1L, 2);

    mockMvc.perform(post("/cart/add")
            .param("productId", "1")
            .param("quantity", "2")
            .param("returnUrl", "/some-page")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/some-page"))
        .andExpect(flash().attribute("errorMessage", "Product with ID 1 not found."));

    verify(cartService).addProductToCart(1L, 2);
  }

  @Test
  @WithAnonymousUser
  void addToCart_withNonexistentId_redirectWithErrorMessage_forAnonymousUser()
      throws Exception {
    assert_addToCart_withNonexistentId_redirectWithErrorMessage();
  }

  @Test
  @WithMockUser
  void addToCart_withNonexistentId_redirectWithErrorMessage_forMockUser() throws Exception {
    assert_addToCart_withNonexistentId_redirectWithErrorMessage();
  }

  void assert_addToCart_withInsufficientStock_redirectWithErrorMessage() throws Exception {
    doThrow(new InsufficientStockException("Not enough stock for the product with ID 1."))
        .when(cartService).addProductToCart(1L, 2);

    mockMvc.perform(post("/cart/add")
            .param("productId", "1")
            .param("quantity", "2")
            .param("returnUrl", "/some-page")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/some-page"))
        .andExpect(flash().attribute("errorMessage",
            "Not enough stock for the product with ID 1."));

    verify(cartService).addProductToCart(1L, 2);
  }

  @Test
  @WithAnonymousUser
  void addToCart_withInsufficientStock_redirectWithErrorMessage_forAnonymousUser()
      throws Exception {
    assert_addToCart_withInsufficientStock_redirectWithErrorMessage();
  }

  @Test
  @WithMockUser
  void addToCart_withInsufficientStock_redirectWithErrorMessage_forMockUser() throws Exception {
    assert_addToCart_withInsufficientStock_redirectWithErrorMessage();
  }

  void assert_addToCart_withValidData_redirectWithSuccessMessage() throws Exception {
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
  @WithAnonymousUser
  void addToCart_withValidData_redirectWithSuccessMessage_forAnonymousUser() throws Exception {
    assert_addToCart_withValidData_redirectWithSuccessMessage();
  }

  @Test
  @WithMockUser
  void addToCart_withValidData_redirectWithSuccessMessage_forMockUser() throws Exception {
    assert_addToCart_withValidData_redirectWithSuccessMessage();
  }

  void assert_updateQuantity_withValidData_redirectToCart() throws Exception {
    mockMvc.perform(put("/cart/update")
            .param("productId", "1")
            .param("quantity", "5")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"));

    verify(cartService).updateProductQuantity(1L, 5);
  }

  @Test
  @WithAnonymousUser
  void updateQuantity_withValidData_redirectToCart_forAnonymousUser() throws Exception {
    assert_updateQuantity_withValidData_redirectToCart();
  }

  @Test
  @WithMockUser
  void updateQuantity_withValidData_redirectToCart_forMockUser() throws Exception {
    assert_updateQuantity_withValidData_redirectToCart();
  }

  void assert_updateQuantity_withNonexistentId_redirectToCart() throws Exception {
    doThrow(new ResourceNotFoundException("Product with ID 1 not found."))
        .when(cartService).updateProductQuantity(1L, 5);

    mockMvc.perform(put("/cart/update")
            .param("productId", "1")
            .param("quantity", "5")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attribute("errorMessage", "Product with ID 1 not found."));

    verify(cartService).updateProductQuantity(1L, 5);
  }

  @Test
  @WithAnonymousUser
  void updateQuantity_withNonexistentId_redirectToCart_forAnonymousUser() throws Exception {
    assert_updateQuantity_withNonexistentId_redirectToCart();
  }

  @Test
  @WithMockUser
  void updateQuantity_withNonexistentId_redirectToCart_forMockUser() throws Exception {
    assert_updateQuantity_withNonexistentId_redirectToCart();
  }

  void assert_updateQuantity_withInsufficientStock_redirectToCart() throws Exception {
    doThrow(new ResourceNotFoundException("Not enough stock for the product with ID 1."))
        .when(cartService).updateProductQuantity(1L, 5);

    mockMvc.perform(put("/cart/update")
            .param("productId", "1")
            .param("quantity", "5")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attribute("errorMessage",
            "Not enough stock for the product with ID 1."));

    verify(cartService).updateProductQuantity(1L, 5);
  }

  @Test
  @WithAnonymousUser
  void updateQuantity_withInsufficientStock_redirectToCart_forAnonymousUser() throws Exception {
    assert_updateQuantity_withInsufficientStock_redirectToCart();
  }

  @Test
  @WithMockUser
  void updateQuantity_withInsufficientStock_redirectToCart_forMockUser() throws Exception {
    assert_updateQuantity_withInsufficientStock_redirectToCart();
  }

  void assert_updateQuantity_withInvalidData_redirectWithErrorMessage() throws Exception {
    mockMvc.perform(put("/cart/update")
            .param("productId", "1")
            .param("quantity", "0")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attribute("errorMessage", "Quantity must be at least 1."));

    verify(cartService, never()).updateProductQuantity(anyLong(), anyInt());
  }

  @Test
  @WithAnonymousUser
  void updateQuantity_withInvalidData_redirectWithErrorMessage_forAnonymousUser() throws Exception {
    assert_updateQuantity_withInvalidData_redirectWithErrorMessage();
  }

  @Test
  @WithMockUser
  void updateQuantity_withInvalidData_redirectWithErrorMessage_forMockUser() throws Exception {
    assert_updateQuantity_withInvalidData_redirectWithErrorMessage();
  }

  void assert_removeItem_redirectToCart() throws Exception {
    mockMvc.perform(delete("/cart/remove")
            .param("productId", "1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"))
        .andExpect(flash().attribute("successMessage", "Product has been removed from your cart."));

    verify(cartService).removeItem(1L);
  }

  @Test
  @WithAnonymousUser
  void removeItem_redirectToCart_forAnonymousUser() throws Exception {
    assert_removeItem_redirectToCart();
  }

  @Test
  @WithMockUser
  void removeItem_redirectToCart_forMockUser() throws Exception {
    assert_removeItem_redirectToCart();
  }
}
