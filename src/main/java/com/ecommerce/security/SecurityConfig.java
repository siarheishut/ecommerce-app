package com.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  private final AccessDeniedHandler customAccessDeniedHandler;
  private final UserDetailsService userDetailsService;

  @Value("${ecommerce.security.remember-me-key}")
  private String rememberMeKey;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .authorizeHttpRequests(configurer ->
            configurer
                .requestMatchers(
                    "/api/auth/**", "/api/cart/**", "/cart/**", "/", "/products/list",
                    "/products/**", "/login", "/logout", "/register", "/error",
                    "/processRegistration", "/forgot-password", "/reset-password",
                    "/access-denied", "/favicon.ico", "/orders/shipping-details",
                    "/orders/place-order", "/orders/confirmation")
                .permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/admin/**")
                .hasRole("ADMIN")
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
        .rememberMe(remember -> remember
            .key(rememberMeKey)
            .tokenValiditySeconds(86400 * 30)
            .userDetailsService(userDetailsService)
        )
        .logout(logout ->
            logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
        );
    http
        .exceptionHandling(exceptions ->
            exceptions.accessDeniedHandler(customAccessDeniedHandler)
        );

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
