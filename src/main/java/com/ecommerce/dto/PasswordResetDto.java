package com.ecommerce.dto;

import com.ecommerce.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@PasswordMatches(passwordFieldName = "password", confirmPasswordFieldName = "confirmPassword")
public class PasswordResetDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String token;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    @NotBlank(message = "Please confirm your new password.")
    private String confirmPassword;
}
