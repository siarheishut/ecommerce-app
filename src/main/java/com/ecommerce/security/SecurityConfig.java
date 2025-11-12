package com.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  private final AccessDeniedHandler customAccessDeniedHandler;
  private final Environment env;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    boolean isTestProfile = Arrays.asList(env.getActiveProfiles()).contains("test");

    if (isTestProfile) {
      http.csrf(csrf -> csrf.disable());
    } else {
      http.csrf(Customizer.withDefaults());
    }

    http
        .headers(headers -> headers
            .addHeaderWriter((_, response) -> {
              response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
              response.setHeader("Pragma", "no-cache");
              response.setHeader("Expires", "0");
            })
            .frameOptions(frame -> frame.deny())
        )
        .authorizeHttpRequests(configurer ->
            configurer
                .requestMatchers(
                    "/api/auth/**", "/cart/**", "/", "/products/list", "/products/**",
                    "/login", "/logout", "/register", "/processRegistration", "/error",
                    "/forgot-password", "/reset-password", "/access-denied", "/favicon.ico",
                    "/orders/shipping-details", "/orders/place-order", "/orders/confirmation")
                .permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
        .formLogin(form ->
            form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
        )
        .logout(logout ->
            logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );
    http
        .exceptionHandling(exceptions ->
            exceptions.accessDeniedHandler(customAccessDeniedHandler)
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SpringSecurityDialect springSecurityDialect() {
    return new SpringSecurityDialect();
  }
}
