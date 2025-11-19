package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDetailsDto implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @NotBlank(message = "First name is required")
  @Size(max = 50, message = "First name cannot be longer than 50 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 50, message = "Last name cannot be longer than 50 characters")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Please enter a valid email address")
  @Size(max = 255, message = "Email cannot be longer than 255 characters")
  private String email;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^(\\+?[0-9]{1,4}[-\\s]?)?(\\([0-9]{1,4}\\)[-\\s]?)?[0-9][0-9-\\s]{5,15}[0-9]$", message = "Please enter a valid phone number.")
  @Size(max = 15, message = "Phone number cannot be longer than 15 characters")
  private String phoneNumber;

  @NotBlank(message = "Address line is required")
  @Size(max = 255, message = "Address line cannot be longer than 255 characters")
  private String addressLine;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City cannot be longer than 100 characters")
  private String city;

  @NotBlank(message = "Country is required")
  @Size(max = 60, message = "Country cannot be longer than 60 characters")
  private String country;

  @NotBlank(message = "Postal code is required")
  @Pattern(regexp = "^[a-zA-Z0-9\\s-]{3,20}$", message = "Please enter a valid postal code.")
  private String postalCode;
}
