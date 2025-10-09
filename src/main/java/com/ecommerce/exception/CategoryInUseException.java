package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
    value = HttpStatus.BAD_REQUEST, reason = "Category is in use and cannot be deleted.")
public class CategoryInUseException extends RuntimeException {
  public CategoryInUseException(String message) {
    super(message);
  }
}
