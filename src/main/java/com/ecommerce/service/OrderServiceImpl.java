package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.OrderHistoryDto;
import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.dto.ShippingDetailsDto;
import com.ecommerce.entity.*;
import com.ecommerce.exception.EmptyCartOrderException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ShoppingCart shoppingCart;
  private final UserService userService;
  private final EmailService emailService;

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
  public Order placeOrder(String username, OrderDto orderDto) {
    User user = userService.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException(
            "User with username " + username + " not found."));

    Order order = new Order();
    order.setUser(user);
    order.setOrderDate(Instant.now());
    order.setStatus(Order.Status.PENDING);

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal totalAmount = BigDecimal.ZERO;

    List<Long> productIds = new ArrayList<>(orderDto.orderItemsList()
        .stream().map(OrderItemDto::getProductId).toList());
    List<Product> productsToUpdate = new ArrayList<>();
    Map<Long, Product> productMap = productRepository.findAllById(productIds)
        .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

    for (OrderItemDto orderItemDto : orderDto.orderItemsList()) {
      Product product = productMap.get(orderItemDto.getProductId());
      if (product == null) {
        throw new ResourceNotFoundException("Product with ID " + orderItemDto.getProductId() +
            " not found.");
      }
      if (orderItemDto.getQuantity() > product.getStockQuantity()) {
        throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
            ".Available: " + product.getStockQuantity() + ".");
      }

      OrderItem orderItem = new OrderItem(order, product, orderItemDto.getQuantity());
      orderItems.add(orderItem);
      totalAmount = totalAmount.add(BigDecimal.valueOf(orderItemDto.getQuantity())
          .multiply(product.getPrice()));

      product.setStockQuantity(product.getStockQuantity() - orderItemDto.getQuantity());
      productsToUpdate.add(product);
    }

    order.addOrderItems(orderItems);
    order.setTotalAmount(totalAmount);
    productRepository.saveAll(productsToUpdate);
    return orderRepository.save(order);
  }

  @Override
  @Transactional
  public void placeOrder(ShippingDetailsDto shippingDetailsDto) {
    if (shoppingCart.getItems().isEmpty()) {
      throw new EmptyCartOrderException("Cannot create order from an empty cart.");
    }

    User currentUser = userService.getCurrentUser();

    Order order = new Order();
    if (currentUser != null) {
      order.setUser(currentUser);
    }

    ShippingDetails shippingDetails = getShippingDetails(shippingDetailsDto);

    order.setOrderDate(Instant.now());
    order.setStatus(Order.Status.PENDING);
    order.setShippingDetails(shippingDetails);
    List<Long> productIds = shoppingCart.getItems().stream()
        .map(item -> item.product().getId())
        .toList();
    List<Product> productsToUpdate = new ArrayList<>();

    Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
        .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<OrderItem> orderItems = new ArrayList<>();
    for (CartSessionItem cartItem : shoppingCart.getItems()) {
      Product product = productMap.get(cartItem.product().getId());
      if (product == null) {
        throw new ResourceNotFoundException("Product with ID " + cartItem.product().getId() +
            " not found.");
      }
      if (product.getStockQuantity() < cartItem.quantity()) {
        throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
            ".Available: " + product.getStockQuantity() + ".");
      }

      OrderItem orderItem = new OrderItem(order, product, cartItem.quantity());
      orderItems.add(orderItem);
      product.setStockQuantity(product.getStockQuantity() - cartItem.quantity());
      productsToUpdate.add(product);
    }

    order.addOrderItems(orderItems);
    order.setTotalAmount(shoppingCart.getTotalAmount());

    productRepository.saveAll(productsToUpdate);
    orderRepository.save(order);
    shoppingCart.clear();
    emailService.sendOrderConfirmationEmail(order);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderHistoryDto> getOrderHistoryForCurrentUser() {
    User currentUser = userService.getCurrentUser();
    if (currentUser == null) {
      return Collections.emptyList();
    }
    List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(currentUser);

    return orders.stream()
        .map(OrderHistoryDto::fromEntity)
        .collect(Collectors.toList());
  }
}
