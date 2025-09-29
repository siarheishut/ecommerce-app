package com.ecommerce.service;

import com.ecommerce.dto.ReviewDto;
import com.ecommerce.dto.ReviewSubmissionDto;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ReviewReadditionException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final UserService userService;

  @Override
  @Transactional(readOnly = true)
  public Page<ReviewDto> getReviewsForProduct(Long productId, Pageable pageable) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));
    Page<Review> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product, pageable);
    return reviews.map(this::convertToDto);
  }

  @Override
  @Transactional
  public void addReview(Long productId, ReviewSubmissionDto reviewDto) {
    User currentUser = userService.getCurrentUser();
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));

    if (reviewRepository.existsByUserAndProduct(currentUser, product)) {
      throw new ReviewReadditionException("You have already reviewed this product.");
    }

    Review review = new Review();
    review.setUser(currentUser);
    review.setProduct(product);
    review.setRating(reviewDto.rating());
    review.setComment(reviewDto.comment());
    product.addReview(review);
  }

  private ReviewDto convertToDto(Review review) {
    return new ReviewDto(
        review.getUser().getUsername(),
        review.getRating(),
        review.getComment(),
        review.getCreatedAt()
    );
  }
}
