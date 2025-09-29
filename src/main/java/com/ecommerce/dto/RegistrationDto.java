package com.ecommerce.dto;

import com.ecommerce.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PasswordMatches(passwordFieldName = "password", confirmPasswordFieldName = "confirmPassword")
public class RegistrationDto {
  @NotEmpty(message = "Username cannot be empty.")
  private String username;

  @NotEmpty(message = "Email cannot be empty.")
  @Email(message = "Please provide a valid email address.")
  private String email;

  @NotEmpty(message = "Password cannot be empty.")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  private String password;

  @NotEmpty(message = "Please confirm your password.")
  private String confirmPassword;
}
