package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.ShippingDetails;
import com.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailServiceImpl emailService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(emailService, "appBaseUrl", "http://localhost:3000");
  }

  @Test
  void whenSendOrderConfirmationEmail_thenMailIsSentWithCorrectDetails() {
    ShippingDetails shippingDetails = mock(ShippingDetails.class);
    when(shippingDetails.getEmail()).thenReturn("customer@gmail.com");
    when(shippingDetails.getFirstName()).thenReturn("Tom");

    Order order = mock(Order.class);
    when(order.getId()).thenReturn(123L);
    when(order.getShippingDetails()).thenReturn(shippingDetails);
    when(order.getOrderDate()).thenReturn(Instant.now());
    when(order.getTotalAmount()).thenReturn(new BigDecimal("100.00"));

    emailService.sendOrderConfirmationEmail(order);

    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getTo()).containsExactly("customer@gmail.com");
    assertThat(sentMessage.getFrom()).isEqualTo("no-reply@ecommerce.com");
    assertThat(sentMessage.getSubject()).isEqualTo("E-commerce Order Confirmation");
    assertThat(sentMessage.getText())
        .contains("Dear Tom,")
        .contains("Order ID: 123")
        .contains("Total Amount: $100.00");
  }

  @Test
  void whenSendPasswordResetEmail_thenMailIsSentWithCorrectLink() {
    User user = mock(User.class);
    when(user.getEmail()).thenReturn("customer@gmail.com");
    String token = "secure-reset-token";

    emailService.sendPasswordResetEmail(user, token);

    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertThat(sentMessage.getTo()).containsExactly("customer@gmail.com");
    assertThat(sentMessage.getFrom()).isEqualTo("no-reply@ecommerce.com");
    assertThat(sentMessage.getSubject()).isEqualTo("Reset Your Password");

    String expectedUrl = "http://localhost:3000/reset-password?token=" + token;
    assertThat(sentMessage.getText())
        .contains("To reset your password, please click the link below:")
        .contains(expectedUrl);
  }
}
