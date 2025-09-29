package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Exceeded addresses number limit.")
public class AddressLimitExceededException extends RuntimeException {
  public AddressLimitExceededException(String message) {
    super(message);
  }
}
