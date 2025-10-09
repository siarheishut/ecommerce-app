package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST, reason = "Restoring an active resource is not allowed.")
public class RestoringActiveResourceException extends RuntimeException {
  public RestoringActiveResourceException(String message) {
    super(message);
  }
}
