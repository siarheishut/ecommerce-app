package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Repeated review Addition.")
public class ReviewReadditionException extends RuntimeException {
  public ReviewReadditionException(String message) {
    super(message);
  }
}
