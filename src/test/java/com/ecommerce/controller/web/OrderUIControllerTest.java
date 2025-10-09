package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.CartItemViewDto;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.entity.User;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.AddressService;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(OrderUIController.class)
@SuppressWarnings("unused")
public class OrderUIControllerTest {

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
  private AddressService addressService;

  @MockitoBean
  private CartService cartService;

  @MockitoBean
  private OrderService orderService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setFirstName("Tom");
    testUser.setLastName("Jerry");
    testUser.setEmail("tom.jerry@example.com");
  }

  @Test
  @WithMockUser
  void shippingDetailsForm_whenCartIsNotEmpty_shouldReturnForm() throws Exception {
    when(cartService.getCartForCurrentUser())
        .thenReturn(new CartViewDto(List.of(new CartItemViewDto(null, 10)), BigDecimal.TEN));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(addressService.getAddressesForCurrentUser()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/orders/shipping-details"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/shipping-details"))
        .andExpect(model().attributeExists("shippingDetails", "addresses"));
  }

  @Test
  @WithMockUser
  void shippingDetailsForm_whenCartIsEmpty_shouldRedirectToCart() throws Exception {
    when(cartService.getCartForCurrentUser())
        .thenReturn(new CartViewDto(Collections.emptyList(), BigDecimal.ZERO));

    mockMvc.perform(get("/orders/shipping-details"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/cart"));
  }

  @Test
  @WithMockUser
  void placeOrder_withValidDetails_shouldPlaceOrderAndRedirect() throws Exception {
    mockMvc.perform(post("/orders/place-order")
            .param("firstName", "Tom")
            .param("lastName", "Jerry")
            .param("email", "tom.jerry@example.com")
            .param("phoneNumber", "1234567")
            .param("addressLine", "street-house")
            .param("city", "City")
            .param("country", "USA")
            .param("postalCode", "12345")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/orders/confirmation"));

    verify(orderService).placeOrder(any());
  }

  @Test
  @WithMockUser
  void placeOrder_withInvalidDetails_shouldRedirectBackToForm() throws Exception {
    mockMvc.perform(post("/orders/place-order")
            .param("firstName", "")
            .param("lastName", "Jerry")
            .param("email", "tom.jerry@example.com")
            .param("addressLine", "street-house")
            .param("city", "City")
            .param("country", "USA")
            .param("postalCode", "12345")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/orders/shipping-details"))
        .andExpect(flash().attributeExists("shippingDetails"))
        .andExpect(flash().attributeExists(BindingResult.MODEL_KEY_PREFIX + "shippingDetails"));

    verify(orderService, never()).placeOrder(any());
  }

  @Test
  @WithMockUser
  void orderHistory_shouldReturnHistoryView() throws Exception {
    List<OrderHistoryDto> orderHistory = List.of(
        new OrderHistoryDto(1L, Instant.now(), null, BigDecimal.TEN, Collections.emptyList())
    );
    when(orderService.getOrderHistoryForCurrentUser()).thenReturn(orderHistory);

    mockMvc.perform(get("/orders/history"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/order-history"))
        .andExpect(model().attribute("orders", orderHistory));
  }

  @Test
  @WithAnonymousUser
  void orderHistory_forAnonymousUser_shouldRedirectToLogin() throws Exception {
    mockMvc.perform(get("/orders/history"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost/login"));
  }

  @Test
  @WithMockUser
  void orderConfirmation_shouldReturnConfirmationView() throws Exception {
    mockMvc.perform(get("/orders/confirmation"))
        .andExpect(status().isOk())
        .andExpect(view().name("public/order-confirmation"));
  }
}
