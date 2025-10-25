package com.ecommerce.repository;

import com.ecommerce.entity.Role;
import jakarta.validation.ConstraintViolationException;
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
class RoleRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RoleRepository roleRepository;

  @Test
  void whenFindByName_withExistingName_returnsOptionalOfRole() {
    Role adminRole = new Role("ROLE_ADMIN");
    entityManager.persistAndFlush(adminRole);

    Optional<Role> foundRole = roleRepository.findByName("ROLE_ADMIN");

    assertThat(foundRole).isPresent();
    assertThat(foundRole.get().getName()).isEqualTo("ROLE_ADMIN");
  }

  @Test
  void whenFindByName_withNonExistingName_returnsEmptyOptional() {
    Optional<Role> notFoundRole = roleRepository.findByName("ROLE_USER");
    assertThat(notFoundRole).isNotPresent();
  }

  @Test
  void whenSaveAndFlush_withDuplicateName_throwsDataIntegrityViolationException() {
    Role adminRole = new Role("ROLE_ADMIN");
    entityManager.persistAndFlush(adminRole);

    Role duplicateRole = new Role("ROLE_ADMIN");

    assertThatThrownBy(() -> roleRepository.saveAndFlush(duplicateRole))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("Unique index or primary key violation");
  }

  @Test
  void whenSaveAndFlush_withNullName_throwsConstraintViolationException() {
    Role roleWithNullName = new Role(null);

    assertThatThrownBy(() -> roleRepository.saveAndFlush(roleWithNullName))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Role name is required.");
  }
}
