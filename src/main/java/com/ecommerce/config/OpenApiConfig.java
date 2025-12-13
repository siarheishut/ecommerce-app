package com.ecommerce.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch(
            "/",
            "/login",
            "/register",
            "/processRegistration",
            "/forgot-password",
            "/reset-password",
            "/products/**",
            "/cart/**",
            "/api/cart/**",
            "/orders/**",
            "/addresses/**",
            "/user-info/**",
            "/my-account",
            "/change-password",
            "/fragments/**"
        )
        .build();
  }

  @Bean
  public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .pathsToMatch("/admin/**")
        .build();
  }
}
