package com.ecommerce.repository;

import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class AddressRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private AddressRepository addressRepository;

  @Test
  void whenFindByUser_withExistingUser_returnsAddressesForThatUserOnly() {
    User user1 = new User();
    user1.setUsername("user1");
    user1.setEmail("user1@email.com");
    user1.setPassword("12345678");
    entityManager.persist(user1);

    User user2 = new User();
    user2.setUsername("user2");
    user2.setEmail("user2@email.com");
    user2.setPassword("87654321");
    entityManager.persist(user2);

    Address address1_user1 = new Address();
    address1_user1.setUser(user1);
    address1_user1.setName("Home");
    address1_user1.setAddressLine("Happy St. 123");
    address1_user1.setCity("Warsaw");
    address1_user1.setCountry("Poland");
    address1_user1.setPostalCode("12-345");
    entityManager.persist(address1_user1);

    Address address2_user1 = new Address();
    address2_user1.setUser(user1);
    address2_user1.setName("Work");
    address2_user1.setAddressLine("Sad St. 321");
    address2_user1.setCity("New York");
    address2_user1.setCountry("USA");
    address2_user1.setPostalCode("54321");
    entityManager.persist(address2_user1);

    Address address1_user2 = new Address();
    address1_user2.setUser(user2);
    address1_user2.setName("Home");
    address1_user2.setAddressLine("Happy St. 321");
    address1_user2.setCity("Praga");
    address1_user2.setCountry("Czech Republic");
    address1_user2.setPostalCode("123 45");
    entityManager.persist(address1_user2);

    entityManager.flush();

    List<Address> foundAddresses = addressRepository.findByUser(user1);

    assertThat(foundAddresses).hasSize(2);
    assertThat(foundAddresses).extracting(Address::getAddressLine)
        .containsExactlyInAnyOrder("Happy St. 123", "Sad St. 321");
  }

  @Test
  void whenSaveAndFlush_withDuplicateUserAndName_throwsDataIntegrityViolationException() {
    User user = new User();
    user.setUsername("user");
    user.setEmail("user@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    Address address1 = new Address();
    address1.setUser(user);
    address1.setName("Home");
    address1.setAddressLine("Line 1");
    address1.setCity("City");
    address1.setCountry("Country");
    address1.setPostalCode("12345");
    entityManager.persistAndFlush(address1);

    Address address2 = new Address();
    address2.setUser(user);
    address2.setName("Home");
    address2.setAddressLine("Line 2");
    address2.setCity("City");
    address2.setCountry("Country");
    address2.setPostalCode("54321");

    assertThatThrownBy(() -> addressRepository.saveAndFlush(address2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void whenSaveAndFlush_withNullUser_throwsConstraintViolationException() {
    Address address = new Address();
    address.setUser(null);
    address.setName("Work");
    address.setAddressLine("Line 1");
    address.setCity("City");
    address.setCountry("Country");
    address.setPostalCode("12345");

    assertThatThrownBy(() -> addressRepository.saveAndFlush(address))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Address must be associated with a user.");
  }

  @Test
  void whenSaveAndFlush_withInvalidPostalCode_throwsConstraintViolationException() {
    User user = new User();
    user.setUsername("user");
    user.setEmail("user@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    assertThatThrownBy(() -> {
      Address address = createValidAddress(user);
      address.setPostalCode("!@#");
      addressRepository.saveAndFlush(address);
    }).isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Please enter a valid postal code.");
  }

  @Test
  void whenSaveAndFlush_withBlankName_throwsConstraintViolationException() {
    User user = new User();
    user.setUsername("user");
    user.setEmail("user@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    Address address = createValidAddress(user);
    address.setName("   ");

    assertThatThrownBy(() -> addressRepository.saveAndFlush(address))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Address name is required");
  }

  @Test
  void whenSaveAndFlush_withBlankCity_throwsConstraintViolationException() {
    User user = new User();
    user.setUsername("user");
    user.setEmail("user@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    Address address = createValidAddress(user);
    address.setCity("   ");

    assertThatThrownBy(() -> addressRepository.saveAndFlush(address))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("City is required");
  }

  @Test
  void whenSaveAndFlush_withBlankCountry_throwsConstraintViolationException() {
    User user = new User();
    user.setUsername("user");
    user.setEmail("user@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    Address address = createValidAddress(user);
    address.setCountry("   ");

    assertThatThrownBy(() -> addressRepository.saveAndFlush(address))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Country is required");
  }

  @Test
  void whenSaveAndFlush_withBlankAddressLine_throwsConstraintViolationException() {
    User user = new User();
    user.setUsername("user");
    user.setEmail("user@email.com");
    user.setPassword("password");
    entityManager.persist(user);

    Address address = createValidAddress(user);
    address.setAddressLine("   ");

    assertThatThrownBy(() -> addressRepository.saveAndFlush(address))
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("Address line is required");
  }

  private Address createValidAddress(User user) {
    Address address = new Address();
    address.setUser(user);
    address.setName("Home");
    address.setAddressLine("123 Main St");
    address.setCity("Anytown");
    address.setCountry("USA");
    address.setPostalCode("12345");
    return address;
  }
}
