package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shipping_details")
@NoArgsConstructor
@Getter
public class ShippingDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @NotBlank(message = "First Name is required")
  @Size(max = 50, message = "First Name cannot be longer than 50 characters")
  @Column(nullable = false)
  private String firstName;

  @Setter
  @NotBlank(message = "Last name is required")
  @Size(max = 50, message = "Last Name cannot be longer than 50 characters")
  @Column(nullable = false)
  private String lastName;

  @Setter
  @NotBlank(message = "Email is required")
  @Size(max = 255, message = "Email cannot be longer than 255 characters")
  @Email(message = "Please enter a valid email address")
  @Column(nullable = false)
  private String email;

  @Setter
  @Column(nullable = false)
  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[+]?[0-9]{1,4}[-\\\\s0-9]*$", message = "Please enter a valid phone number.")
  @Size(max = 15, message = "Phone number cannot be longer than 15 characters")
  private String phoneNumber;

  @Setter
  @NotBlank(message = "Address line is required")
  @Size(max = 255, message = "Address line cannot be longer than 255 characters")
  @Column(nullable = false)
  private String addressLine;

  @Setter
  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City cannot be longer than 100 characters")
  @Column(nullable = false)
  private String city;

  @Setter
  @NotBlank(message = "Country is required")
  @Size(max = 60, message = "Country cannot be longer than 60 characters")
  @Column(nullable = false)
  private String country;

  @Setter
  @NotBlank(message = "Postal code is required")
  @Pattern(regexp = "^[a-zA-Z0-9\\s-]{3,20}$", message = "Please enter a valid postal code.")
  @Column(nullable = false)
  private String postalCode;
}
