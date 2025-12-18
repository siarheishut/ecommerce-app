package com.ecommerce.service;

import com.ecommerce.dto.OrderEmailDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;

public interface EmailService {
  void sendOrderConfirmationEmail(OrderEmailDto order);

  void sendPasswordResetEmail(User user, String token);
}
