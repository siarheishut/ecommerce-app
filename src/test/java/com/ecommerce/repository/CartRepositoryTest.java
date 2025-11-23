package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.sql.init.mode=never"})
class CartRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private CartRepository cartRepository;

  @Test
  void whenFindByUser_withExistingCart_returnsOptionalOfCart() {
    User user = createUser("user");
    Cart cart = new Cart();
    cart.setUser(user);
    entityManager.persist(cart);
    entityManager.flush();

    Optional<Cart> foundCart = cartRepository.findByUser(user);

    assertThat(foundCart).isPresent();
    assertThat(foundCart.get().getUser()).isEqualTo(user);
  }

  @Test
  void whenFindByUser_withNoCart_returnsEmptyOptional() {
    User user = createUser("user");
    Optional<Cart> foundCart = cartRepository.findByUser(user);
    assertThat(foundCart).isNotPresent();
  }

  @Test
  void whenFindByUserWithLock_withExistingCart_returnsCartWithItems() {
    User user = createUser("user");
    Product product1 = createProduct("p1", BigDecimal.TEN);
    Product product2 = createProduct("p2", BigDecimal.ONE);

    Cart cart = new Cart();
    cart.setUser(user);
    entityManager.persist(cart);

    CartItem item1 = new CartItem(cart, product1, 2);
    CartItem item2 = new CartItem(cart, product2, 5);
    cart.getItems().add(item1);
    cart.getItems().add(item2);
    entityManager.persist(cart);

    entityManager.flush();
    entityManager.clear();

    Optional<Cart> foundCart = cartRepository.findByUserWithLock(user);

    assertThat(foundCart).isPresent();
    assertThat(foundCart.get().getUser().getUsername()).isEqualTo("user");
    assertThat(foundCart.get().getItems()).hasSize(2);
    assertThat(foundCart.get().getItems())
        .extracting(item -> item.getProduct().getName())
        .containsExactlyInAnyOrder("p1", "p2");
  }

  @Test
  void whenFindByUserWithLock_withNoCart_returnsEmpty() {
    User user = createUser("user");
    Optional<Cart> foundCart = cartRepository.findByUserWithLock(user);
    assertThat(foundCart).isNotPresent();
  }

  private User createUser(String username) {
    User user = new User();
    user.setUsername(username);
    user.setEmail(username + "@test.com");
    user.setPassword("password");
    user.setEnabled(true);
    return entityManager.persist(user);
  }

  private Product createProduct(String name, BigDecimal price) {
    Product product = new Product();
    product.setName(name);
    product.setPrice(price);
    product.setStockQuantity(100);
    return entityManager.persist(product);
  }
}
