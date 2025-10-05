package com.ecommerce.repository;

import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.entity.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
public class OrderRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private OrderRepository orderRepository;

  private User user;
  private Product product;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setUsername("testuser");
    user.setEmail("test@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    product = new Product();
    product.setName("Test Product");
    product.setPrice(BigDecimal.TEN);
    entityManager.persist(product);
  }

  @Test
  void whenFindOrderHistoryByUser_withExistingOrders_returnsDtoListSortedByDateDesc() {
    Instant now = Instant.now();
    Order order1 = new Order();
    order1.setUser(user);
    order1.setOrderDate(now.minus(1, ChronoUnit.DAYS));
    order1.setStatus(Order.Status.SHIPPED);
    order1.setTotalAmount(BigDecimal.TWO);
    order1.addOrderItems(List.of(new OrderItem(order1, product, 1)));
    order1.setShippingDetails(createShippingDetails());
    entityManager.persist(order1);

    Order order2 = new Order();
    order2.setUser(user);
    order2.setOrderDate(now);
    order2.setStatus(Order.Status.PENDING);
    order2.setTotalAmount(BigDecimal.ONE);
    order2.addOrderItems(List.of(new OrderItem(order2, product, 2)));
    order2.setShippingDetails(createShippingDetails());
    entityManager.persist(order2);

    List<OrderHistoryDto> foundOrders = orderRepository.findOrderHistoryByUser(user);

    assertThat(foundOrders).hasSize(2);
    assertThat(foundOrders).extracting(OrderHistoryDto::orderDate)
        .containsExactly(now, now.minus(1, ChronoUnit.DAYS));
  }

  @Test
  void whenSaveAndFlush_withNoOrderItems_throwsConstraintViolationException() {
    Order order = createValidOrder();
    order.getOrderItems().clear();

    assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Order must contain at least one item.");
  }

  @Test
  void whenSaveAndFlush_withNullShippingDetails_throwsConstraintViolationException() {
    Order order = createValidOrder();
    order.setShippingDetails(null);

    assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Shipping details are required.");
  }

  @Test
  void whenSaveAndFlush_withNegativeTotalAmount_throwsConstraintViolationException() {
    Order order = createValidOrder();
    order.setTotalAmount(new BigDecimal("-1.00"));

    assertThatThrownBy(() -> orderRepository.saveAndFlush(order))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Total amount cannot be negative.");
  }

  private Order createValidOrder() {
    Order order = new Order();
    order.setUser(user);
    order.setOrderDate(Instant.now());
    order.setStatus(Order.Status.PENDING);
    order.setTotalAmount(BigDecimal.TEN);
    order.setShippingDetails(createShippingDetails());

    OrderItem item = new OrderItem(order, product, 1);
    order.addOrderItems(List.of(item));

    return order;
  }

  private ShippingDetails createShippingDetails() {
    ShippingDetails details = new ShippingDetails();
    details.setFirstName("Tom");
    details.setLastName("Jerry");
    details.setEmail("tom.jerry@email.com");
    details.setPhoneNumber("123456789");
    details.setAddressLine("Anystreet");
    details.setCity("Anytown");
    details.setCountry("USA");
    details.setPostalCode("12345");
    return details;
  }
}
