package com.ecommerce.dto;

import com.ecommerce.entity.User;
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
public class UserInfoDto implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Size(max = 50, message = "First name cannot be longer than 50 characters.")
  private String firstName;

  @Size(max = 50, message = "Last name cannot be longer than 50 characters.")
  private String lastName;

  @Pattern(regexp = "^$|^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$", message = "Please enter a valid phone number.")
  private String phoneNumber;

  static public UserInfoDto fromEntity(User user) {
    return new UserInfoDto(user.getFirstName(), user.getLastName(), user.getPhoneNumber());
  }
}
