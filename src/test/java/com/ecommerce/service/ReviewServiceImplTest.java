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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private ReviewServiceImpl reviewService;

  @Test
  void whenGetReviewsForProduct_withExistingProduct_returnsReviewPage() {
    Long productId = 1L;
    Pageable pageable = PageRequest.of(0, 10);
    Product product = new Product();

    User user = new User();
    user.setUsername("testuser");

    Review review = mock(Review.class);
    Instant reviewTime = Instant.now();
    when(review.getUser()).thenReturn(user);
    when(review.getRating()).thenReturn(5);
    when(review.getComment()).thenReturn("Great product!");
    when(review.getCreatedAt()).thenReturn(reviewTime);

    Page<Review> expectedReviews = new PageImpl<>(List.of(review), pageable, 1);

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(reviewRepository.findByProductOrderByCreatedAtDesc(product, pageable))
        .thenReturn(expectedReviews);

    Page<ReviewDto> result = reviewService.getReviewsForProduct(productId, pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);

    ReviewDto reviewDto = result.getContent().getFirst();
    assertThat(reviewDto.authorUsername()).isEqualTo("testuser");
    assertThat(reviewDto.rating()).isEqualTo(5);
    assertThat(reviewDto.comment()).isEqualTo("Great product!");
    assertThat(reviewDto.createdAt()).isEqualTo(reviewTime);
  }

  @Test
  void whenGetReviewsForProduct_withNonExistentProduct_throwsResourceNotFoundException() {
    Long productId = 99L;
    Pageable pageable = PageRequest.of(0, 10);
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> reviewService.getReviewsForProduct(productId, pageable)
    );

    assertThat(exception.getMessage()).isEqualTo("Product with ID 99 not found.");
    verify(reviewRepository, never()).findByProductOrderByCreatedAtDesc(any(), any());
  }

  @Test
  void whenAddReview_withValidData_addsReviewToProduct() {
    Long productId = 1L;
    ReviewSubmissionDto reviewDto = new ReviewSubmissionDto(1, "Poor quality");
    User currentUser = new User();
    Product product = mock(Product.class);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(reviewRepository.existsByUserAndProduct(currentUser, product)).thenReturn(false);

    reviewService.addReview(productId, reviewDto);

    ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
    verify(product).addReview(reviewCaptor.capture());

    Review addedReview = reviewCaptor.getValue();
    assertThat(addedReview.getUser()).isSameAs(currentUser);
    assertThat(addedReview.getProduct()).isSameAs(product);
    assertThat(addedReview.getRating()).isEqualTo(1);
    assertThat(addedReview.getComment()).isEqualTo("Poor quality");
  }

  @Test
  void whenAddReview_withNonExistentProduct_throwsResourceNotFoundException() {
    Long productId = 1L;
    ReviewSubmissionDto reviewDto = new ReviewSubmissionDto(5, "Excellent");
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> reviewService.addReview(productId, reviewDto)
    );

    assertThat(exception.getMessage()).isEqualTo("Product with ID 1 not found.");
  }

  @Test
  void whenAddReview_forAlreadyReviewedProduct_throwsReviewReadditionException() {
    Long productId = 1L;
    ReviewSubmissionDto reviewDto = new ReviewSubmissionDto(1, "Bad");
    User currentUser = new User();
    Product product = mock(Product.class);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(reviewRepository.existsByUserAndProduct(currentUser, product)).thenReturn(true);

    ReviewReadditionException exception = assertThrows(
        ReviewReadditionException.class,
        () -> reviewService.addReview(productId, reviewDto)
    );

    verify(product, never()).addReview(any(Review.class));
    assertThat(exception.getMessage()).isEqualTo("You have already reviewed this product.");
  }
}
