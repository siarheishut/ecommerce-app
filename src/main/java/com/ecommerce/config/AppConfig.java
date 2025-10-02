package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
public class AppConfig {

  @Bean
  public Lock cartLock() {
    return new ReentrantLock();
  }
}
