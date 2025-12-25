package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "username")
@Table(name = "users")
public class User {
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Address> addresses = new ArrayList<>();

  @ManyToMany(fetch = FetchType.EAGER,
      cascade = {
          CascadeType.DETACH,
          CascadeType.MERGE,
          CascadeType.PERSIST,
          CascadeType.REFRESH})
  @JoinTable(name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private final Set<Role> roles = new HashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(length = 50)
  @Size(max = 50, message = "First name cannot be longer than 50 characters.")
  private String firstName;

  @Setter
  @Column(length = 50)
  @Size(max = 50, message = "Last name cannot be longer than 50 characters.")
  private String lastName;

  @Setter
  @Column(length = 15)
  @Size(max = 15, message = "Phone number cannot be longer than 15 characters.")
  private String phoneNumber;

  @Setter
  @Column(unique = true, nullable = false)
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
  private String username;

  @Setter
  @Column(nullable = false)
  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  private String password;

  @Setter
  @Column(nullable = false, unique = true)
  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address.")
  private String email;

  @Setter
  @Column(nullable = false)
  private boolean enabled;

  @Setter
  @Column
  private String passwordResetToken;

  @Setter
  @Column
  private Instant passwordResetTokenExpiry;

  public void addRole(Role role) {
    roles.add(role);
  }

  public void addAddress(Address address) {
    addresses.add(address);
    address.setUser(this);
  }

  public void removeAddress(Address address) {
    addresses.remove(address);
    address.setUser(null);
  }
}
