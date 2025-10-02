package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "User is not authenticated.")
public class UserNotAuthenticatedException extends RuntimeException {
  public UserNotAuthenticatedException(String message) {
    super(message);
  }
}
