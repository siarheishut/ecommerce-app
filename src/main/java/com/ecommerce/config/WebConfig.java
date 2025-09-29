package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final StringToCategoryConverter stringToCategoryConverter;

  @Autowired
  public WebConfig(StringToCategoryConverter stringToCategoryConverter) {
    this.stringToCategoryConverter = stringToCategoryConverter;
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(stringToCategoryConverter);
  }
}
