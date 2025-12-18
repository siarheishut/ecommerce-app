package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.EmptyCartOrderException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

  @Mock
  private OrderRepository orderRepository;
  @Mock
  private ProductRepository productRepository;
  @Mock
  private CartService cartService;
  @Mock
  private UserService userService;
  @Mock
  private EmailService emailService;
  @Mock
  private ExecutorService notificationExecutor;

  @InjectMocks
  private OrderServiceImpl orderService;

  @Captor
  private ArgumentCaptor<Order> orderCaptor;
  @Captor
  private ArgumentCaptor<List<Product>> productListCaptor;

  @Test
  void whenFindById_findSuccessfully() {
    Long orderId = 1L;
    Optional<Order> expectedOrder = Optional.of(new Order());
    when(orderRepository.findById(orderId)).thenReturn(expectedOrder);

    Optional<Order> actualOrder = orderService.findById(orderId);

    assertThat(actualOrder).isEqualTo(expectedOrder);
    verify(orderRepository).findById(orderId);
  }

  @Test
  void whenPlaceOrderFromCart_withValidDataForGuest_createsOrderAndClearsCart() {
    doAnswer(invocation -> {
      Runnable r = invocation.getArgument(0);
      r.run();
      return null;
    }).when(notificationExecutor).execute(any(Runnable.class));

    ShippingDetailsDto shippingDto = new ShippingDetailsDto(
        "Tom", "Sawyer", "tom.sawyer@gmail.com", "123456789", "Some Address line",
        "Some City", "Some Country", "12345");

    Product product = mock(Product.class);
    when(product.getId()).thenReturn(1L);
    when(product.getName()).thenReturn("Toy");
    when(product.getPrice()).thenReturn(new BigDecimal("25.00"));
    when(product.getStockQuantity()).thenReturn(5);

    ProductViewDto productDto = ProductViewDto.fromEntity(product, 2);
    CartItemViewDto cartItemView = new CartItemViewDto(productDto);
    CartViewDto cartView = new CartViewDto(List.of(cartItemView), new BigDecimal("50.00"));

    when(cartService.getCartForCurrentUser()).thenReturn(cartView);
    when(userService.getCurrentUser()).thenReturn(null);
    when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(product));

    orderService.placeOrder(shippingDto);

    verify(orderRepository).save(orderCaptor.capture());
    Order savedOrder = orderCaptor.getValue();

    assertThat(savedOrder.getUser()).isNull();
    assertThat(savedOrder.getStatus()).isEqualTo(Order.Status.PENDING);
    assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("50.00");
    assertThat(savedOrder.getShippingDetails().getFirstName()).isEqualTo("Tom");
    assertThat(savedOrder.getOrderItems()).hasSize(1);
    assertThat(savedOrder.getOrderItems().getFirst().getQuantity()).isEqualTo(2);

    verify(product).setStockQuantity(3);
    verify(productRepository).saveAll(productListCaptor.capture());

    verify(cartService).removeItem(1L);
    verify(emailService).sendOrderConfirmationEmail(OrderEmailDto.fromEntity(savedOrder));
  }

  @Test
  void whenPlaceOrderFromCart_withValidDataForAuthenticatedUser_createsOrderAndClearsCart() {
    doAnswer(invocation -> {
      Runnable r = invocation.getArgument(0);
      r.run();
      return null;
    }).when(notificationExecutor).execute(any(Runnable.class));

    ShippingDetailsDto shippingDto = new ShippingDetailsDto(
        "Tom", "Sawyer", "tom.sawyer@gmail.com", "123456789", "Some Address line",
        "Some City", "Some Country", "12345");
    User currentUser = new User();

    Product product = mock(Product.class);
    when(product.getId()).thenReturn(1L);
    when(product.getStockQuantity()).thenReturn(10);

    ProductViewDto productDto = ProductViewDto.fromEntity(product, 1);
    CartItemViewDto cartItemView = new CartItemViewDto(productDto);
    CartViewDto cartView = new CartViewDto(List.of(cartItemView), new BigDecimal("25.00"));

    when(cartService.getCartForCurrentUser()).thenReturn(cartView);
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(productRepository.findAllById(any())).thenReturn(List.of(product));
    orderService.placeOrder(shippingDto);

    verify(orderRepository).save(orderCaptor.capture());
    Order savedOrder = orderCaptor.getValue();

    assertThat(savedOrder.getUser()).isEqualTo(currentUser);
    assertThat(savedOrder.getShippingDetails().getFirstName()).isEqualTo("Tom");
    verify(product).setStockQuantity(9);

    verify(cartService).removeItem(1L);
    verify(emailService).sendOrderConfirmationEmail(OrderEmailDto.fromEntity(savedOrder));
  }

  @Test
  void whenPlaceOrderFromCart_withEmptyCart_throwsEmptyCartOrderException() {
    when(cartService.getCartForCurrentUser())
        .thenReturn(new CartViewDto(Collections.emptyList(), BigDecimal.ZERO));

    ShippingDetailsDto shippingDto = new ShippingDetailsDto();

    EmptyCartOrderException exception = assertThrows(
        EmptyCartOrderException.class,
        () -> orderService.placeOrder(shippingDto)
    );
    verify(orderRepository, never()).save(any());
    assertThat(exception.getMessage()).isEqualTo("Cannot create order from an empty cart.");
  }

  @Test
  void whenPlaceOrderFromCart_withMissingProduct_throwsResourceNotFoundException() {
    ShippingDetailsDto shippingDto = new ShippingDetailsDto();

    Product productInCart = mock(Product.class);
    when(productInCart.getId()).thenReturn(1L);
    ProductViewDto productDto = ProductViewDto.fromEntity(productInCart, 1);
    CartItemViewDto cartItemView = new CartItemViewDto(productDto);
    CartViewDto cartView = new CartViewDto(List.of(cartItemView), BigDecimal.TEN);

    when(cartService.getCartForCurrentUser()).thenReturn(cartView);
    when(userService.getCurrentUser()).thenReturn(null);

    when(productRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> orderService.placeOrder(shippingDto)
    );
    assertThat(exception.getMessage()).contains("Product with ID 1 not found.");
  }

  @Test
  void whenGetOrderHistoryForCurrentUser_withGuestUser_returnsEmptyList() {
    when(userService.getCurrentUser()).thenReturn(null);
    List<OrderHistoryDto> history = orderService.getOrderHistoryForCurrentUser();

    assertThat(history).isEmpty();
    verify(orderRepository, never()).findOrderHistoryByUser(any());
  }
}
