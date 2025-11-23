package com.ecommerce.security;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final ShoppingCart sessionCart;
  private final CartService cartService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
    mergeSessionCartToUserCart();

    String redirectUrl = request.getParameter("redirectUrl");
    if (redirectUrl != null && !redirectUrl.isBlank()) {
      getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    } else {
      super.onAuthenticationSuccess(request, response, authentication);
    }
  }

  private void mergeSessionCartToUserCart() {
    if (!sessionCart.getItems().isEmpty()) {
      log.info("Merged session cart to user database cart for user.");
      for (CartSessionItem item : sessionCart.getItems()) {
        try {
          cartService.addProductToCart(item.product().id(), item.quantity());
        } catch (Exception e) {
          log.error("Failed to merge product ID {} to user cart: {}", item.product().id(), e.getMessage());
        }
      }
      sessionCart.clear();
    }
  }
}
