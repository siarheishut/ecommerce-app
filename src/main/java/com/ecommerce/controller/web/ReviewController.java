package com.ecommerce.controller.web;

import com.ecommerce.dto.ReviewSubmissionDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ReviewReadditionException;
import com.ecommerce.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Review Management", description = "Operations for submitting product reviews.")
@Controller
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;

  @Operation(summary = "Submit a review", description = "Adds a review for a specific product.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "302",
          description = "Success: Review submitted, redirects to product page."),
      @ApiResponse(
          responseCode = "302",
          description = "Failure: Validation error, duplicate review, or product not found, " +
              "redirects to product page with error.")
  })
  @PostMapping
  public String addReview(
      @Parameter(description = "ID of the product being reviewed.")
      @PathVariable("productId") Long productId,

      @Valid
      @ModelAttribute("newReview") ReviewSubmissionDto reviewDto,

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
