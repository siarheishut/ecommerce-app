package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = {"user", "name"})
@Table(name = "addresses", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @NotNull(message = "Address must be associated with a user.")
  private User user;

  @Setter
  @Column(nullable = false, length = 50)
  @NotBlank(message = "Address name is required.")
  @Size(max = 50, message = "Address name cannot be longer than 50 characters.")
  private String name;

  @Setter
  @Column(nullable = false, length = 60)
  @NotBlank(message = "Country is required.")
  @Size(max = 60, message = "Country cannot be longer than 60 characters.")
  private String country;

  @Setter
  @Column(nullable = false, length = 100)
  @NotBlank(message = "City is required.")
  @Size(max = 100, message = "City cannot be longer than 100 characters.")
  private String city;

  @Setter
  @Column(nullable = false, length = 255)
  @NotBlank(message = "Address line is required.")
  @Size(max = 255, message = "Address line cannot be longer than 255 characters.")
  private String addressLine;

  @Setter
  @Column(nullable = false, length = 20)
  @NotBlank(message = "Postal code is required.")
  @Pattern(regexp = "^[a-zA-Z0-9\\s-]{3,20}$", message = "Please enter a valid postal code.")
  private String postalCode;
}
