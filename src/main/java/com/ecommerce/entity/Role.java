package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "roles")
public class Role implements Serializable {

  @Serial
  private static final long serialVersionUID = 7L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @Column(nullable = false, unique = true)
  @NotBlank(message = "Role name is required.")
  private String name;

  public Role(String name) {
    this.name = name;
  }
}
