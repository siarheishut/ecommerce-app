package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AddressDto implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private Long id;

  @NotBlank(message = "Address name is required")
  @Size(max = 50, message = "Address name cannot be longer than 50 characters")
  private String name;

  @NotBlank(message = "Country is required")
  @Size(max = 60, message = "Country cannot be longer than 60 characters")
  private String country;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City cannot be longer than 100 characters")
  private String city;

  @NotBlank(message = "Address line is required")
  @Size(max = 255, message = "Address line cannot be longer than 255 characters")
  private String addressLine;

  @NotBlank(message = "Postal code is required")
  @Pattern(regexp = "^[a-zA-Z0-9\\s-]{3,20}$", message = "Please enter a valid postal code.")
  private String postalCode;
}
