package com.ecommerce.controller.web;

import com.ecommerce.config.StringToCategoryConverter;
import com.ecommerce.dto.ReviewSubmissionDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ReviewReadditionException;
import com.ecommerce.security.CustomAccessDeniedHandler;
import com.ecommerce.security.CustomAuthenticationSuccessHandler;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.SecurityConfig;
import com.ecommerce.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(ReviewController.class)
@WithMockUser
@SuppressWarnings("unused")
public class ReviewControllerTest {

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
  private ReviewService reviewService;

  @MockitoBean
  private StringToCategoryConverter stringToCategoryConverter;

  @Test
  void addReview_withValidData_redirectsWithSuccess() throws Exception {
    mockMvc.perform(post("/products/1/reviews")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("rating", "5")
            .param("comment", "Excellent product!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/products/1"))
        .andExpect(flash().attribute("successMessage", "Review submitted successfully!"));

    verify(reviewService).addReview(eq(1L), any(ReviewSubmissionDto.class));
  }

  @Test
  void addReview_withInvalidData_redirectsWithBindingErrors() throws Exception {
    mockMvc.perform(post("/products/1/reviews")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("rating", "99")
            .param("comment", ""))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/products/1"))
        .andExpect(flash().attributeExists("newReview", "org.springframework.validation.BindingResult.newReview"));
  }

  @Test
  void addReview_forNonExistentProduct_redirectsWithError() throws Exception {
    doThrow(new ResourceNotFoundException("Product not found."))
        .when(reviewService).addReview(eq(999L), any(ReviewSubmissionDto.class));

    mockMvc.perform(post("/products/999/reviews")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("rating", "4")
            .param("comment", "Good product"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/products/999"))
        .andExpect(flash().attribute("errorMessage", "Product not found."));
  }

  @Test
  void addReview_whenAlreadyReviewed_redirectsWithError() throws Exception {
    doThrow(new ReviewReadditionException("User attempted to re-review product."))
        .when(reviewService).addReview(eq(1L), any(ReviewSubmissionDto.class));

    mockMvc.perform(post("/products/1/reviews")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("rating", "3")
            .param("comment", "Trying to review again"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/products/1"))
        .andExpect(flash().attribute("errorMessage", "User attempted to re-review product."));
  }
}
