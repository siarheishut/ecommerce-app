package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.EmptyCartOrderException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UserNotAuthenticatedException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final CartService cartService;
  private final UserService userService;
  private final EmailService emailService;
  private final ExecutorService notificationExecutor;

  private static ShippingDetails getShippingDetails(ShippingDetailsDto shippingDetailsDto) {
    ShippingDetails shippingDetails = new ShippingDetails();
    shippingDetails.setFirstName(shippingDetailsDto.getFirstName());
    shippingDetails.setLastName(shippingDetailsDto.getLastName());
    shippingDetails.setEmail(shippingDetailsDto.getEmail());
    shippingDetails.setPhoneNumber(shippingDetailsDto.getPhoneNumber());
    shippingDetails.setAddressLine(shippingDetailsDto.getAddressLine());
    shippingDetails.setCity(shippingDetailsDto.getCity());
    shippingDetails.setCountry(shippingDetailsDto.getCountry());
    shippingDetails.setPostalCode(shippingDetailsDto.getPostalCode());
    return shippingDetails;
  }

  @Override
  public Optional<Order> findById(Long id) {
    return orderRepository.findById(id);
  }

  @Override
  @Transactional
  public void placeOrder(ShippingDetailsDto shippingDetailsDto) {
    CartViewDto cartView = cartService.getCartForCurrentUser();

    if (cartView.items().isEmpty()) {
      throw new EmptyCartOrderException("Cannot create order from an empty cart.");
    }

    Order order = new Order();
    try {
      order.setUser(userService.getCurrentUser());
    } catch (UserNotAuthenticatedException _) {
    }

    ShippingDetails shippingDetails = getShippingDetails(shippingDetailsDto);

    order.setOrderDate(Instant.now());
    order.setStatus(Order.Status.PENDING);
    order.setShippingDetails(shippingDetails);
    List<Long> productIds = cartView.items().stream()
        .map(item -> item.product().id())
        .toList();
    List<Product> productsToUpdate = new ArrayList<>();

    Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItemViewDto cartItem : cartView.items()) {
      Long productId = cartItem.product().id();
      int quantity = cartItem.product().inCartQuantity();
      Product product = productMap.get(cartItem.product().id());

      if (product == null) {
        throw new ResourceNotFoundException("Product with ID " + cartItem.product().id() +
            " not found.");
      }
      if (product.getStockQuantity() < quantity) {
        throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
            ".Available: " + product.getStockQuantity() + ".");
      }

      OrderItem orderItem = new OrderItem(order, product, quantity);
      orderItems.add(orderItem);
      product.setStockQuantity(product.getStockQuantity() - quantity);
      productsToUpdate.add(product);
    }

    order.addOrderItems(orderItems);
    order.setTotalAmount(cartView.totalAmount());

    productRepository.saveAll(productsToUpdate);
    orderRepository.save(order);
    for (Long id : productIds) {
      cartService.removeItem(id);
    }

    OrderEmailDto emailDto = OrderEmailDto.fromEntity(order);

    Runnable emailTask = () -> {
      CompletableFuture.runAsync(() ->
              emailService.sendOrderConfirmationEmail(emailDto), notificationExecutor)
          .exceptionally(ex -> {
            log.error("Failed to send email for order {}", emailDto.orderId(), ex);
            return null;
          });
    };

    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          log.info("Transaction committed. Triggering email for order {}", emailDto.orderId());
          emailTask.run();
        }
      });
    } else {
      // Fallback for Unit Tests
      log.warn("No transaction active. Sending email immediately (Test).");
      emailTask.run();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderHistoryDto> getOrderHistoryForCurrentUser() {
    User currentUser = userService.getCurrentUser();
    if (currentUser == null) {
      return Collections.emptyList();
    }
    List<OrderHistoryDto> orders = orderRepository.findOrderHistoryByUser(currentUser);
    if (orders.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> orderIds = orders.stream().map(OrderHistoryDto::orderId).toList();
    Map<Long, List<OrderHistoryItemDto>> itemsByOrderId =
        orderRepository.findOrderHistoryItemsByOrderIds(orderIds).stream()
            .collect(Collectors.groupingBy(OrderHistoryItemDto::orderId));

    return orders.stream().map(order -> new OrderHistoryDto(
        order.orderId(), order.orderDate(), order.status(), order.totalAmount(),
        itemsByOrderId.getOrDefault(order.orderId(), Collections.emptyList())
    )).collect(Collectors.toList());
  }
}
