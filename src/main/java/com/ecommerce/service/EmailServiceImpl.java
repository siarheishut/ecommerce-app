package com.ecommerce.service;

import com.ecommerce.dto.OrderEmailDto;
import com.ecommerce.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;
  @Value("${app.base-url}")
  private String appBaseUrl;

  @Override
  public void sendOrderConfirmationEmail(OrderEmailDto order) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("no-reply@ecommerce.com");
    message.setTo(order.email());
    message.setSubject("E-commerce Order Confirmation");

    String emailBody = String.format(
        """
            Dear %s,
            
            Thank you for your order! We've received it and will process it shortly.
            
            Order Details:
            Order ID: %d
            Order Date: %s
            Total Amount: $%.2f
            
            Thank you for shopping with us!
            The E-commerce Team""",
        order.firstName(),
        order.orderId(),
        order.orderDate(),
        order.totalPrice()
    );

    message.setText(emailBody);
    mailSender.send(message);
  }

  @Override
  public void sendPasswordResetEmail(User user, String token) {
    String url = UriComponentsBuilder.fromUriString(appBaseUrl)
        .path("/reset-password")
        .queryParam("token", token)
        .toUriString();

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("no-reply@ecommerce.com");
    message.setTo(user.getEmail());
    message.setSubject("Reset Your Password");
    message.setText("To reset your password, please click the link below:\n" + url);

    mailSender.send(message);
  }
}
