package com.ecommerce.repository;

import com.ecommerce.AbstractIntegrationTest;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
public class ReviewRepositoryTest extends AbstractIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ReviewRepository reviewRepository;

  private User user1;
  private User user2;
  private Product product1;
  private Product product2;

  @BeforeEach
  void setUp() {
    user1 = new User();
    user1.setUsername("user1");
    user1.setEmail("user1@test.com");
    user1.setPassword("password");
    entityManager.persist(user1);

    user2 = new User();
    user2.setUsername("user2");
    user2.setEmail("user2@test.com");
    user2.setPassword("password");
    entityManager.persist(user2);

    product1 = new Product();
    product1.setName("Product 1");
    product1.setPrice(BigDecimal.TEN);
    product1.setStockQuantity(5);
    entityManager.persist(product1);

    product2 = new Product();
    product2.setName("Product 2");
    product2.setPrice(BigDecimal.ONE);
    product2.setStockQuantity(10);
    entityManager.persist(product2);
  }

  @Test
  void whenFindByProductOrderByCreatedAtDesc_withExistingReviews_returnsPagedReviewsSortedByDate() {
    Review review1 = new Review();
    review1.setUser(user1);
    review1.setProduct(product1);
    review1.setRating(3);
    entityManager.persist(review1);

    try {
      Thread.sleep(10);
    } catch (InterruptedException _) {
    }

    Review review2 = new Review();
    review2.setUser(user2);
    review2.setProduct(product1);
    review2.setRating(5);
    entityManager.persist(review2);

    Page<Review> reviewPage = reviewRepository.findByProductOrderByCreatedAtDesc(product1, PageRequest.of(0, 5));

    assertThat(reviewPage.getContent()).hasSize(2);
    assertThat(reviewPage.getContent()).containsExactly(review2, review1);
  }

  @Test
  void whenExistsByUserAndProduct_withVariousScenarios_returnsCorrectBoolean() {
    Review review = new Review();
    review.setUser(user1);
    review.setProduct(product1);
    review.setRating(4);
    entityManager.persist(review);

    assertThat(reviewRepository.existsByUserAndProduct(user1, product1)).isTrue();
    assertThat(reviewRepository.existsByUserAndProduct(user1, product2)).isFalse();
    assertThat(reviewRepository.existsByUserAndProduct(user2, product1)).isFalse();
  }

  @Test
  void whenSaveAndFlush_withDuplicateUserAndProduct_throwsDataIntegrityViolationException() {
    Review existingReview = new Review();
    existingReview.setUser(user1);
    existingReview.setProduct(product1);
    existingReview.setRating(4);
    entityManager.persistAndFlush(existingReview);

    Review duplicateReview = new Review();
    duplicateReview.setUser(user1);
    duplicateReview.setProduct(product1);
    duplicateReview.setRating(5);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(duplicateReview))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void whenSaveAndFlush_withNullUser_throwsConstraintViolationException() {
    Review reviewWithNullUser = new Review();
    reviewWithNullUser.setUser(null);
    reviewWithNullUser.setProduct(product1);
    reviewWithNullUser.setRating(5);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(reviewWithNullUser))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Review must be associated with a user");
  }

  @Test
  void whenSaveAndFlush_withNullProduct_throwsConstraintViolationException() {
    Review reviewWithNullProduct = new Review();
    reviewWithNullProduct.setUser(user1);
    reviewWithNullProduct.setProduct(null);
    reviewWithNullProduct.setRating(5);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(reviewWithNullProduct))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Review must be associated with a product.");
  }

  @Test
  void whenSaveAndFlush_withNullRating_throwsConstraintViolationException() {
    Review reviewWithNullRating = new Review();
    reviewWithNullRating.setUser(user1);
    reviewWithNullRating.setProduct(product1);
    reviewWithNullRating.setRating(null);

    assertThatThrownBy(() -> reviewRepository.saveAndFlush(reviewWithNullRating))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("A rating is required.");
  }
}
