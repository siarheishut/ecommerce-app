package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.dto.ShippingDetailsDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.EmptyCartOrderException;
import com.ecommerce.exception.InsufficientStockException;
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
import java.util.Map;
import java.util.Optional;

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
    private ShoppingCart shoppingCart;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;
    @Captor
    private ArgumentCaptor<List<Product>> productListCaptor;

    @Test
    void whenFindById_thenRepositoryMethodIsCalled() {
        Long orderId = 1L;
        Optional<Order> expectedOrder = Optional.of(new Order());
        when(orderRepository.findById(orderId)).thenReturn(expectedOrder);

        Optional<Order> actualOrder = orderService.findById(orderId);

        assertThat(actualOrder).isEqualTo(expectedOrder);
        verify(orderRepository).findById(orderId);
    }

    @Test
    void whenPlaceOrderFromDto_withValidData_createsOrderAndReducesStock() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);

        Product product1 = mock(Product.class);
        when(product1.getId()).thenReturn(1L);
        when(product1.getStockQuantity()).thenReturn(10);
        when(product1.getName()).thenReturn("Chandelier");
        when(product1.getPrice()).thenReturn(new BigDecimal("1200.00"));

        Product product2 = mock(Product.class);
        when(product2.getId()).thenReturn(2L);
        when(product2.getStockQuantity()).thenReturn(50);
        when(product2.getName()).thenReturn("Box");
        when(product2.getPrice()).thenReturn(new BigDecimal("25.00"));

        OrderItemDto itemDto1 = new OrderItemDto();
        itemDto1.setProductId(product1.getId());
        itemDto1.setQuantity(1);
        OrderItemDto itemDto2 = new OrderItemDto();
        itemDto2.setProductId(product2.getId());
        itemDto2.setQuantity(2);
        OrderDto orderDto = new OrderDto(List.of(itemDto1, itemDto2));

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(product1, product2));

        orderService.placeOrder(username, orderDto);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getUser()).isEqualTo(user);
        assertThat(savedOrder.getStatus()).isEqualTo(Order.Status.PENDING);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("1250.00");
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        verify(product1).setStockQuantity(9);
        verify(product2).setStockQuantity(48);

        verify(productRepository).saveAll(productListCaptor.capture());
        List<Product> updatedProducts = productListCaptor.getValue();
        assertThat(updatedProducts).hasSize(2);
    }

    @Test
    void whenPlaceOrderFromCart_withValidDataForGuest_createsOrderAndClearsCart() {
        ShippingDetailsDto shippingDto = new ShippingDetailsDto(
            "Tom", "Sawyer", "tom.sawyer@gmail.com", "123456789", "Some Address line",
            "Some City", "Some Country", "12345");

        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Toy");
        when(product.getPrice()).thenReturn(new BigDecimal("25.00"));
        when(product.getStockQuantity()).thenReturn(5);

        CartSessionItem cartItem = new CartSessionItem(product, 2);

        when(shoppingCart.getItems()).thenReturn(List.of(cartItem));
        when(userService.getCurrentUser()).thenReturn(null);
        when(shoppingCart.getTotalAmount()).thenReturn(new BigDecimal("50.00"));
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

        verify(shoppingCart).clear();
        verify(emailService).sendOrderConfirmationEmail(savedOrder);
    }

    @Test
    void whenPlaceOrderFromCart_withValidDataForAuthenticatedUser_createsOrderAndClearsCart() {
        ShippingDetailsDto shippingDto = new ShippingDetailsDto(
            "Tom", "Sawyer", "tom.sawyer@gmail.com", "123456789", "Some Address line",
            "Some City", "Some Country", "12345");
        User currentUser = new User();

        Product product = mock(Product.class);

        CartSessionItem cartItem = new CartSessionItem(product, 1);

        when(product.getId()).thenReturn(1L);
        when(product.getStockQuantity()).thenReturn(10);
        when(shoppingCart.getItems()).thenReturn(List.of(cartItem));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(productRepository.findAllById(any())).thenReturn(List.of(product));

        orderService.placeOrder(shippingDto);

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getUser()).isEqualTo(currentUser);
        assertThat(savedOrder.getShippingDetails().getFirstName()).isEqualTo("Tom");
        verify(product).setStockQuantity(9);

        verify(shoppingCart).clear();
        verify(emailService).sendOrderConfirmationEmail(savedOrder);
    }

    @Test
    void whenPlaceOrderFromCart_withEmptyCart_throwsEmptyCartOrderException() {
        when(shoppingCart.getItems()).thenReturn(Collections.emptyList());
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
        CartSessionItem cartItem = new CartSessionItem(productInCart, 1);

        when(productInCart.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(null);
        when(shoppingCart.getItems()).thenReturn(List.of(cartItem));
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
        verify(orderRepository, never()).findByUserOrderByOrderDateDesc(any());
    }
}
