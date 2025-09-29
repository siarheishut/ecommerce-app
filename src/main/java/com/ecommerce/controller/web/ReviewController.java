package com.ecommerce.controller.web;

import com.ecommerce.dto.ReviewSubmissionDto;
import com.ecommerce.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    if (bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newReview", bindingResult);
      redirectAttributes.addFlashAttribute("newReview", reviewDto);
      return "redirect:/products/" + productId;
    }

    reviewService.addReview(productId, reviewDto);
    redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");

    return "redirect:/products/" + productId;
  }
}
