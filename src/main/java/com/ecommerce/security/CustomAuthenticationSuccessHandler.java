package com.ecommerce.security;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  public CustomAuthenticationSuccessHandler() {
    this.setTargetUrlParameter("redirectUrl");
    this.setDefaultTargetUrl("/");
    this.setAlwaysUseDefaultTargetUrl(false);
  }
}
