package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Cannot create order from an empty cart.")
public class EmptyCartOrderException extends IllegalStateException {
  public EmptyCartOrderException(String message) {
    super(message);
  }
}
