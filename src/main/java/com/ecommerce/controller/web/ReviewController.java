package com.ecommerce.controller.web;

import com.ecommerce.dto.ReviewSubmissionDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ReviewReadditionException;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;

  @PostMapping
  public String addReview(@PathVariable("productId") Long productId,
                          @Valid @ModelAttribute("newReview") ReviewSubmissionDto reviewDto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
    String redirectUrl = "redirect:/products/" + productId;

    if (bindingResult.hasErrors()) {
      log.warn("Review form has validation errors.");
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newReview",
          bindingResult);
      redirectAttributes.addFlashAttribute("newReview", reviewDto);
      return redirectUrl;
    }

    try {
      reviewService.addReview(productId, reviewDto);
      log.info("Review submitted successfully for product with ID {}.", productId);
      redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
    } catch (ResourceNotFoundException e) {
      log.warn("User attempted to review a non-existent product with ID {}.", productId);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (ReviewReadditionException e) {
      log.warn("User attempted to re-review product with ID {}.", productId);
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }

    return redirectUrl;
  }
}
