package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;

public interface EmailService {
  void sendOrderConfirmationEmail(Order order);

  void sendPasswordResetEmail(User user, String token);
}
