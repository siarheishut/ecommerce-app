package com.ecommerce.service;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewSubmissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
  Page<ReviewDto> getReviewsForProduct(Long productId, Pageable pageable);

  void addReview(Long productId, ReviewSubmissionDto reviewDto);
}
