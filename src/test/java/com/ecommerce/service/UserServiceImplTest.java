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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class )
public class UserServiceImplTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private EmailService emailService;

  @InjectMocks
  UserServiceImpl userService;

  @Test
  public void whenRegisterUser_andRoleUserNotFound_throwConfigurationException() {
    RegistrationDto registrationDto = new RegistrationDto();
    registrationDto.setUsername("Tom");
    registrationDto.setPassword("12345678");
    registrationDto.setConfirmPassword("12345678");
    registrationDto.setEmail("ImTom@gmail.com");

    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

    ConfigurationException exception = assertThrows(
        ConfigurationException.class,
        () -> userService.registerUser(registrationDto)
    );

    assertEquals("'ROLE_USER' not found in the database.", exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void whenRegisterUser_withValidData_savesUser() {
    RegistrationDto registrationDto = new RegistrationDto();
    registrationDto.setUsername("Tom");
    registrationDto.setPassword("12345678");
    registrationDto.setEmail("ImTom@gmail.com");

    Role userRole = new Role("ROLE_USER");
    String encodedPassword = "encodedPassword123";

    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn(encodedPassword);

    userService.registerUser(registrationDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getUsername()).isEqualTo(registrationDto.getUsername());
    assertThat(savedUser.getEmail()).isEqualTo(registrationDto.getEmail());
    assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
    assertThat(savedUser.isEnabled()).isTrue();
    assertThat(savedUser.getRoles()).contains(userRole);
  }

  @Test
  public void whenUpdateUser_withValidData_updateSuccessfully() {
    UserInfoDto userInfoDto = new UserInfoDto();
    userInfoDto.setFirstName("Tom");
    userInfoDto.setLastName("Holand");
    userInfoDto.setPhoneNumber("+48123456789");

    User existingUser = new User();
    existingUser.setUsername("current_user");
    existingUser.setFirstName("OldName");
    existingUser.setLastName("OldSurname");
    existingUser.setPhoneNumber("+48987654321");

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(securityContext);
    when(authentication.getPrincipal()).thenReturn(existingUser.getUsername());
    when(userRepository.findByUsername(existingUser.getUsername()))
        .thenReturn(Optional.of(existingUser));

    userService.updateCurrentUserInfo(userInfoDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getFirstName()).isEqualTo("Tom");
    assertThat(savedUser.getLastName()).isEqualTo("Holand");
    assertThat(savedUser.getPhoneNumber()).isEqualTo("+48123456789");
  }

  @Test
  public void whenUpdateUser_withNullAuthentication_throwUserNotAuthenticatedException() {
    UserInfoDto userInfoDto = new UserInfoDto();
    userInfoDto.setFirstName("Tom");
    userInfoDto.setLastName("Holand");
    userInfoDto.setPhoneNumber("+48123456789");

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(securityContext);
    assertThrows(
        UserNotAuthenticatedException.class,
        () -> userService.updateCurrentUserInfo(userInfoDto)
    );
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void whenUpdateUser_withUnauthenticated_throwUserNotAuthenticatedException() {
    UserInfoDto userInfoDto = new UserInfoDto();
    userInfoDto.setFirstName("Tom");
    userInfoDto.setLastName("Holand");
    userInfoDto.setPhoneNumber("+48123456789");

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);
    SecurityContextHolder.setContext(securityContext);
    assertThrows(
        UserNotAuthenticatedException.class,
        () -> userService.updateCurrentUserInfo(userInfoDto)
    );
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void whenUpdateUser_withAnonymousUser_throwUserNotAuthenticatedException() {
    UserInfoDto userInfoDto = new UserInfoDto();
    userInfoDto.setFirstName("Tom");
    userInfoDto.setLastName("Holand");
    userInfoDto.setPhoneNumber("+48123456789");

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(securityContext);
    when(authentication.getPrincipal()).thenReturn("anonymousUser");
    assertThrows(
        UserNotAuthenticatedException.class,
        () -> userService.updateCurrentUserInfo(userInfoDto)
    );
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void whenUpdateUser_withUserNotFound_throwResourceNotFoundException() {
    UserInfoDto userInfoDto = new UserInfoDto();
    userInfoDto.setFirstName("Tom");
    userInfoDto.setLastName("Holand");
    userInfoDto.setPhoneNumber("+48123456789");

    User existingUser = new User();
    existingUser.setUsername("Unknown");
    existingUser.setFirstName("OldName");
    existingUser.setLastName("OldSurname");
    existingUser.setPhoneNumber("+48987654321");

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(securityContext);
    when(authentication.getPrincipal()).thenReturn(existingUser.getUsername());
    when(userRepository.findByUsername(existingUser.getUsername())).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
        ResourceNotFoundException.class,
        () -> userService.updateCurrentUserInfo(userInfoDto)
    );
    verify(userRepository, never()).save(any(User.class));
    assertEquals("Authenticated user 'Unknown' not found in the database.", exception.getMessage());
  }

  @Test
  public void whenCreatePasswordResetToken_savesTokenAndTriggersEmail() {
    User user = new User();
    user.setEmail("test@example.com");

    userService.createPasswordResetTokenForUser(user);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getPasswordResetToken()).isNotNull();
    assertThat(savedUser.getPasswordResetTokenExpiry()).isNotNull();

    ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetEmail(userCaptor.capture(), tokenCaptor.capture());

    assertThat(userCaptor.getValue()).isSameAs(user);
    assertThat(tokenCaptor.getValue()).isEqualTo(savedUser.getPasswordResetToken());
  }

  @Test
  public void whenValidatePasswordResetToken_validateSuccessfully() {
    User user = new User();
    String token = "token123";
    user.setPasswordResetTokenExpiry(Instant.MAX);
    user.setPasswordResetToken(token);

    when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
    assertThat(userService.validatePasswordResetToken("token123")).isTrue();
  }

  @Test
  public void whenValidatePasswordResetToken_withNonExistingToken_returnFalse() {
    User user = new User();
    String token = "token123";
    user.setPasswordResetTokenExpiry(Instant.MAX);
    user.setPasswordResetToken("token321");

    when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.empty());
    assertThat(userService.validatePasswordResetToken("token123")).isFalse();
  }

  @Test
  public void whenValidatePasswordResetToken_withExpiredToken_returnFalse() {
    User user = new User();
    String token = "token123";
    user.setPasswordResetTokenExpiry(Instant.MIN);
    user.setPasswordResetToken(token);

    when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
    assertThat(userService.validatePasswordResetToken("token123")).isFalse();
  }

  @Test
  public void whenChangeUserPassword_changeSuccessfully() {
    User user = new User();
    user.setPassword("old password");
    user.setPasswordResetToken("token123");
    user.setPasswordResetTokenExpiry(Instant.MAX);

    when(passwordEncoder.encode("new password")).thenReturn("new encoded password");

    userService.changeUserPassword(user, "new password");

    assertThat(user.getPassword()).isEqualTo("new encoded password");
    assertThat(user.getPasswordResetToken()).isNull();
    assertThat(user.getPasswordResetTokenExpiry()).isNull();
    verify(userRepository).save(user);
  }

  @Test
  public void whenChangeCurrentUserPassword_withValidData_changeSuccessfully() {
    User user = new User();
    user.setUsername("Current user");

    ChangePasswordDto changePasswordDto = new ChangePasswordDto();
    changePasswordDto.setCurrentPassword("current password");
    changePasswordDto.setNewPassword("new password");
    changePasswordDto.setConfirmPassword("new password");

    Authentication authentication = mock(Authentication.class);
    SecurityContext secContext = mock(SecurityContext.class);
    when(secContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(secContext);
    when(authentication.getPrincipal()).thenReturn(user.getUsername());
    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword()))
        .thenReturn(true);
    when(passwordEncoder.encode(changePasswordDto.getNewPassword())).thenReturn("encoded password");

    boolean result = userService.changeCurrentUserPassword(changePasswordDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(userCaptor.getValue()).isSameAs(user);
    assertThat(savedUser.getPassword()).isEqualTo("encoded password");
    assertThat(result).isTrue();
  }

  @Test
  public void whenChangeCurrentUserPassword_withInvalidCurrentPassword_returnFalse() {
    User user = new User();
    user.setUsername("Current user");

    ChangePasswordDto changePasswordDto = new ChangePasswordDto();
    changePasswordDto.setCurrentPassword("current password");
    changePasswordDto.setNewPassword("new password");
    changePasswordDto.setConfirmPassword("new password");

    Authentication authentication = mock(Authentication.class);
    SecurityContext secContext = mock(SecurityContext.class);
    when(secContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    SecurityContextHolder.setContext(secContext);
    when(authentication.getPrincipal()).thenReturn(user.getUsername());
    when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword()))
        .thenReturn(false);

    boolean result = userService.changeCurrentUserPassword(changePasswordDto);

    verify(userRepository, never()).save(any(User.class));
    assertThat(result).isFalse();
  }
}
