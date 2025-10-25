package com.ecommerce.repository;

import com.ecommerce.entity.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.sql.init.mode=never"})
class UserRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    User testUser = new User();
    testUser.setUsername("user");
    testUser.setEmail("user@email.com");
    testUser.setPassword("password123");
    testUser.setPasswordResetToken("reset-token-123");
    entityManager.persistAndFlush(testUser);
  }

  @Test
  void whenFindByUsername_withExistingUser_returnsOptionalOfUser() {
    Optional<User> foundUser = userRepository.findByUsername("user");
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo("user");
  }

  @Test
  void whenFindByUsername_withNonExistentUser_returnsEmptyOptional() {
    Optional<User> notFoundUser = userRepository.findByUsername("nonexistent");
    assertThat(notFoundUser).isNotPresent();
  }

  @Test
  void whenFindByEmail_withExistingUser_returnsOptionalOfUser() {
    Optional<User> foundUser = userRepository.findByEmail("user@email.com");
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo("user@email.com");
  }

  @Test
  void whenFindByEmail_withNonExistentUser_returnsEmptyOptional() {
    Optional<User> notFoundUser = userRepository.findByEmail("nonexistent@email.com");
    assertThat(notFoundUser).isNotPresent();
  }

  @Test
  void whenExistsByUsername_withExistingUser_returnsTrue() {
    boolean exists = userRepository.existsByUsername("user");
    assertThat(exists).isTrue();
  }

  @Test
  void whenExistsByUsername_withNonExistentUser_returnsFalse() {
    boolean notExists = userRepository.existsByUsername("nonexistent");
    assertThat(notExists).isFalse();
  }

  @Test
  void whenExistsByEmail_withExistingUser_returnsTrue() {
    boolean exists = userRepository.existsByEmail("user@email.com");
    assertThat(exists).isTrue();
  }

  @Test
  void whenExistsByEmail_withNonExistentUser_returnsFalse() {
    boolean notExists = userRepository.existsByEmail("nonexistent@email.com");
    assertThat(notExists).isFalse();
  }

  @Test
  void whenFindByPasswordResetToken_withValidToken_returnsOptionalOfUser() {
    Optional<User> foundUser = userRepository.findByPasswordResetToken("reset-token-123");
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getPasswordResetToken()).isEqualTo("reset-token-123");
  }

  @Test
  void whenFindByPasswordResetToken_withInvalidToken_returnsEmptyOptional() {
    Optional<User> notFoundUser = userRepository.findByPasswordResetToken("invalid-token");
    assertThat(notFoundUser).isNotPresent();
  }

  @Test
  void whenSaveAndFlush_withDuplicateUsername_throwsDataIntegrityViolationException() {
    User duplicateUser = new User();
    duplicateUser.setUsername("user");
    duplicateUser.setEmail("another@email.com");
    duplicateUser.setPassword("anotherpassword");
    duplicateUser.setEnabled(true);

    assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void whenSaveAndFlush_withDuplicateEmail_throwsDataIntegrityViolationException() {
    User duplicateUser = new User();
    duplicateUser.setUsername("anotheruser");
    duplicateUser.setEmail("user@email.com");
    duplicateUser.setPassword("anotherpassword");
    duplicateUser.setEnabled(true);

    assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void whenSaveAndFlush_withNullUsername_throwsConstraintViolationException() {
    User userWithNullUsername = new User();
    userWithNullUsername.setUsername(null);
    userWithNullUsername.setEmail("new-user-1@email.com");
    userWithNullUsername.setPassword("password123");
    userWithNullUsername.setEnabled(true);

    assertThatThrownBy(() -> userRepository.saveAndFlush(userWithNullUsername))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Username is required");
  }

  @Test
  void whenSaveAndFlush_withNullEmail_throwsConstraintViolationException() {
    User userWithNullEmail = new User();
    userWithNullEmail.setUsername("new-user-2");
    userWithNullEmail.setEmail(null);
    userWithNullEmail.setPassword("password123");
    userWithNullEmail.setEnabled(true);

    assertThatThrownBy(() -> userRepository.saveAndFlush(userWithNullEmail))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Email is required");
  }

  @Test
  void whenSaveAndFlush_withNullPassword_throwsConstraintViolationException() {
    User userWithNullPassword = new User();
    userWithNullPassword.setUsername("new-user-3");
    userWithNullPassword.setEmail("new-user-3@email.com");
    userWithNullPassword.setPassword(null);
    userWithNullPassword.setEnabled(true);

    assertThatThrownBy(() -> userRepository.saveAndFlush(userWithNullPassword))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Password is required");
  }
}
