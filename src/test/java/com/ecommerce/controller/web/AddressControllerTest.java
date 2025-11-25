package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.AddressDto;
import com.ecommerce.exception.AddressLimitExceededException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.AddressService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(AddressController.class)
@WithMockUser
@SuppressWarnings("unused")
public class AddressControllerTest {

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
  void whenSaveAddress_withValidData_redirectsWithSuccess() throws Exception {
    mockMvc.perform(post("/addresses/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "test_name")
            .param("addressLine", "test_street")
            .param("city", "test_city")
            .param("postalCode", "12345")
            .param("country", "test_country"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attribute("addressSuccess", "Address saved successfully!"));

    ArgumentCaptor<AddressDto> captor = ArgumentCaptor.forClass(AddressDto.class);
    verify(addressService).saveAddress(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("test_name");
    assertThat(captor.getValue().getAddressLine()).isEqualTo("test_street");
    assertThat(captor.getValue().getCity()).isEqualTo("test_city");
    assertThat(captor.getValue().getPostalCode()).isEqualTo("12345");
    assertThat(captor.getValue().getCountry()).isEqualTo("test_country");
  }

  @Test
  void whenSaveAddress_withNameTaken_redirectsWithValidationError() throws Exception {
    when(addressService.isNameTakenByUser(eq("Work"), any())).thenReturn(true);

    mockMvc.perform(post("/addresses/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Work")
            .param("addressLine", "test_street")
            .param("city", "test_city")
            .param("postalCode", "12345")
            .param("country", "test_country"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attributeExists(
            "address", "org.springframework.validation.BindingResult.address", "openAddressModal"));

    verify(addressService, never()).saveAddress(any(AddressDto.class));
  }

  @Test
  void whenSaveAddress_withInvalidData_redirectsWithBindingErrors() throws Exception {
    mockMvc.perform(post("/addresses/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "")
            .param("addressLine", "test_street")
            .param("city", "test_city")
            .param("postalCode", "12345")
            .param("country", "test_country"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attributeExists("address", "org.springframework.validation.BindingResult.address", "openAddressModal"));

    verify(addressService, never()).saveAddress(any(AddressDto.class));
  }

  @Test
  void whenSaveAddress_andLimitExceeded_redirectsWithError() throws Exception {
    String errorMessage = "Address limit exceeded.";
    doThrow(new AddressLimitExceededException(errorMessage)).when(addressService).saveAddress(any(AddressDto.class));

    mockMvc.perform(post("/addresses/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "test_name")
            .param("addressLine", "test_street")
            .param("city", "test_city")
            .param("postalCode", "12345")
            .param("country", "test_country"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attribute("addressError", errorMessage));
  }

  @Test
  void whenSaveAddress_andResourceNotFound_redirectsWithError() throws Exception {
    doThrow(new ResourceNotFoundException("Address with ID 1 not found."))
        .when(addressService).saveAddress(any(AddressDto.class));

    mockMvc.perform(post("/addresses/save")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("id", "1")
            .param("name", "test_name")
            .param("addressLine", "test_street")
            .param("city", "test_city")
            .param("postalCode", "12345")
            .param("country", "test_country"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attribute("addressError", "Address with ID 1 not found."));
  }

  @Test
  void whenDeleteAddress_withValidId_redirectsWithSuccess() throws Exception {
    mockMvc.perform(post("/addresses/delete/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attribute("addressSuccess", "Address deleted successfully!"));

    verify(addressService).deleteAddress(1L);
  }

  @Test
  void whenDeleteAddress_withInvalidId_redirectsWithError() throws Exception {
    doThrow(new ResourceNotFoundException("Cannot delete non-existent address."))
        .when(addressService).deleteAddress(1L);

    mockMvc.perform(post("/addresses/delete/1")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/my-account"))
        .andExpect(flash().attribute("addressError", "Cannot delete non-existent address."));

    verify(addressService).deleteAddress(1L);
  }
}
