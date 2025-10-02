package com.ecommerce.service;

import com.ecommerce.dto.ChangePasswordDto;
import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.dto.UserInfoDto;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ConfigurationException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UserNotAuthenticatedException;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final EmailService emailService;

  @Override
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public void registerUser(RegistrationDto registrationDto) {
    User user = new User();
    user.setUsername(registrationDto.getUsername());
    user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
    user.setEmail(registrationDto.getEmail());
    user.setEnabled(true);
    Role userRole = roleRepository.findByName("ROLE_USER")
        .orElseThrow(() -> new ConfigurationException("'ROLE_USER' not found in the database."));
    user.addRole(userRole);
    userRepository.save(user);
  }

  @Override
  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() ||
        "anonymousUser".equals(authentication.getPrincipal())) {
      throw new UserNotAuthenticatedException(
          "No authenticated user found in the security context.");
    }

    String username;
    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
      username = ((UserDetails) principal).getUsername();
    } else {
      username = principal.toString();
    }

    return findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Authenticated user '" + username + "' not found in the database."));
  }

  @Override
  @Transactional
  public void updateCurrentUserInfo(UserInfoDto userInfoDto) {
    User currentUser = getCurrentUser();
    currentUser.setFirstName(userInfoDto.getFirstName());
    currentUser.setLastName(userInfoDto.getLastName());
    currentUser.setPhoneNumber(userInfoDto.getPhoneNumber());
    userRepository.save(currentUser);
  }

  @Override
  @Transactional
  public void createPasswordResetTokenForUser(User user) {
    String token = UUID.randomUUID().toString();
    user.setPasswordResetToken(token);
    user.setPasswordResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
    userRepository.save(user);
    emailService.sendPasswordResetEmail(user, token);
  }

  @Override
  public boolean validatePasswordResetToken(String token) {
    return userRepository.findByPasswordResetToken(token)
        .map(user -> user.getPasswordResetTokenExpiry().isAfter(Instant.now()))
        .orElse(false);
  }

  @Override
  public Optional<User> findByPasswordResetToken(String token) {
    return userRepository.findByPasswordResetToken(token);
  }

  @Override
  public void changeUserPassword(User user, String password) {
    user.setPassword(passwordEncoder.encode(password));
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);
    userRepository.save(user);
  }

  @Override
  @Transactional
  public boolean changeCurrentUserPassword(ChangePasswordDto changePasswordDto) {
    User currentUser = getCurrentUser();
    if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), currentUser.getPassword())) {
      return false;
    }
    currentUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
    userRepository.save(currentUser);
    return true;
  }
}
