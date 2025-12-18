package com.ecommerce.service;

import com.ecommerce.dto.OrderEmailDto;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  void whenSendOrderConfirmationEmail_mailIsSentWithCorrectDetails() {
    OrderEmailDto orderEmailDto = new OrderEmailDto(
        123L,
        "customer@gmail.com",
        "Tom",
        Instant.now(),
        new BigDecimal("100.00")
    );

    emailService.sendOrderConfirmationEmail(orderEmailDto);

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
  void whenSendPasswordResetEmail_mailIsSentWithCorrectLink() {
    User user = new User();
    user.setEmail("customer@gmail.com");
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
