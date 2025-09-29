package com.ecommerce.service;

import com.ecommerce.dto.ChangePasswordDto;
import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  Optional<User> findById(Long id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  void registerUser(RegistrationDto registrationDto);

  User getCurrentUser();

  void updateCurrentUserInfo(UserInfoDto userInfoDto);

  void createPasswordResetTokenForUser(User user);

  boolean validatePasswordResetToken(String token);

  Optional<User> findByPasswordResetToken(String token);

  void changeUserPassword(User user, String password);

  boolean changeCurrentUserPassword(ChangePasswordDto changePasswordDto);
}
