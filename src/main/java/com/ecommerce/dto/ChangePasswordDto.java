package com.ecommerce.dto;

import com.ecommerce.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PasswordMatches(passwordFieldName = "newPassword", confirmPasswordFieldName = "confirmPassword")
public class ChangePasswordDto {
  @NotBlank(message = "Current password is required.")
  private String currentPassword;

  @NotBlank(message = "New password is required.")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  private String newPassword;

  @NotBlank(message = "Please confirm your new password.")
  private String confirmPassword;
}
