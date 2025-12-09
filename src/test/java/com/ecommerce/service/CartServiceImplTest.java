package com.ecommerce.service;

import com.ecommerce.cart.CartSessionItem;
import com.ecommerce.cart.ShoppingCart;
import com.ecommerce.dto.CartViewDto;
import com.ecommerce.dto.ProductViewDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

  @Mock
  private ProductRepository productRepository;
  @Mock
  private CartRepository cartRepository;
  @Mock
  private ShoppingCart sessionCart;
  @Mock
  private UserService userService;
  @Mock
  private Lock cartLock;

  @InjectMocks
  private CartServiceImpl cartService;

  private Product product;
  private User user;

  @BeforeEach
  void setUp() {
    user = mock(User.class);
    product = mock(Product.class);
  }

  @Test
  void whenAddProductToCart_withNonExistentProduct_throwsResourceNotFoundException() {
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    Exception exception = assertThrows(ResourceNotFoundException.class, () ->
        cartService.addProductToCart(99L, 1)
    );

    assertThat(exception.getMessage()).isEqualTo("Product with ID 99 not found.");
  }

  @Nested
  class AuthenticatedUserTests {

    @BeforeEach
    void setUp() {
      user = mock(User.class);
      product = mock(Product.class);
      when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void whenAddProductToCart_forNewItem_createsCartAndAddsItem() {
      Cart userCart = mock(Cart.class);

      when(product.getId()).thenReturn(1L);
      when(product.getStockQuantity()).thenReturn(10);

      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartRepository.findByUserWithLock(user)).thenReturn(Optional.empty());
      when(cartRepository.save(any())).thenReturn(userCart);
      when(userService.getCurrentUser()).thenReturn(user);

      cartService.addProductToCart(product.getId(), 2);

      verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void whenAddProductToCart_withInsufficientStock_throwsException() {
      when(product.getId()).thenReturn(1L);
      when(product.getStockQuantity()).thenReturn(10);
      when(product.getName()).thenReturn("Laptop");

      Cart cart = new Cart();
      cart.setUser(user);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartRepository.findByUserWithLock(user)).thenReturn(Optional.of(cart));

      Exception exception = assertThrows(InsufficientStockException.class, () ->
          cartService.addProductToCart(product.getId(), 11)
      );

      assertThat(exception.getMessage()).contains("Not enough stock for Laptop");
    }

    @Test
    void whenUpdateProductQuantity_updatesItemInDbCart() {
      when(product.getId()).thenReturn(1L);
      when(product.getStockQuantity()).thenReturn(10);

      CartItem cartItem = new CartItem(null, product, 2);
      Cart cart = new Cart();
      cart.setUser(user);
      cart.setItems(new ArrayList<>(List.of(cartItem)));

      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartRepository.findByUserWithLock(user)).thenReturn(Optional.of(cart));

      cartService.updateProductQuantity(product.getId(), 5);

      assertThat(cartItem.getQuantity()).isEqualTo(5);
      verify(cartRepository).save(cart);
    }

    @Test
    void whenUpdateProductQuantity_toZero_removesItemFromDbCart() {
      CartItem cartItem = new CartItem(null, product, 2);
      Cart cart = new Cart();
      cart.setUser(user);
      cart.setItems(new ArrayList<>(List.of(cartItem)));

      when(cartRepository.findByUserWithLock(user)).thenReturn(Optional.of(cart));

      cartService.updateProductQuantity(product.getId(), 0);

      assertThat(cart.getItems()).isEmpty();
      verify(cartRepository).save(cart);
    }

    @Test
    void whenGetCart_returnsDbCartView() {
      when(product.getId()).thenReturn(1L);
      when(product.getPrice()).thenReturn(BigDecimal.valueOf(1500));

      CartItem cartItem = new CartItem(null, product, 2);
      Cart cart = new Cart();
      cart.setUser(user);
      cart.setItems(new ArrayList<>(List.of(cartItem)));

      when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

      CartViewDto cartView = cartService.getCartForCurrentUser();

      assertThat(cartView.items()).hasSize(1);
      assertThat(cartView.items().stream().toList().get(0).product().id()).isEqualTo(1L);
      assertThat(cartView.items().stream().toList().get(0).product().inCartQuantity()).isEqualTo(2);
      assertThat(cartView.totalAmount()).isEqualByComparingTo("3000.00");
    }
  }

  @Nested
  class AnonymousUserTests {

    @BeforeEach
    void setUp() {
      when(userService.getCurrentUser()).thenThrow(new RuntimeException("No user logged in"));
    }

    @Test
    void whenAddProductToCart_addsItemToSessionCart() {
      when(product.getId()).thenReturn(1L);
      when(product.getStockQuantity()).thenReturn(10);

      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(sessionCart.getItems()).thenReturn(List.of());

      cartService.addProductToCart(product.getId(), 1);

      verify(sessionCart).addItem(any(ProductViewDto.class), eq(1));
      verify(cartLock).lock();
      verify(cartLock).unlock();
    }

    @Test
    void whenAddProductToCart_withInsufficientStock_throwsException() {
      when(product.getId()).thenReturn(1L);
      when(product.getName()).thenReturn("Laptop");
      when(product.getStockQuantity()).thenReturn(2);

      CartSessionItem sessionItem = new CartSessionItem(ProductViewDto.fromEntity(product, 1), 1);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(sessionCart.getItems()).thenReturn(List.of(sessionItem));

      Exception exception = assertThrows(InsufficientStockException.class, () ->
          cartService.addProductToCart(product.getId(), 2)
      );

      assertThat(exception.getMessage()).isEqualTo("Not enough stock for Laptop. Available: 1.");
      verify(cartLock).lock();
      verify(cartLock).unlock();
    }

    @Test
    void whenUpdateProductQuantity_updatesItemInSessionCart() {
      when(product.getId()).thenReturn(1L);
      when(product.getStockQuantity()).thenReturn(5);

      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

      cartService.updateProductQuantity(product.getId(), 5);

      verify(sessionCart).updateItemQuantity(product.getId(), 5);
      verify(cartLock).lock();
      verify(cartLock).unlock();
    }

    @Test
    void whenUpdateProductQuantity_toNegative_removesItemFromSessionCart() {
      cartService.updateProductQuantity(1L, -1);

      verify(sessionCart).removeItem(1L);
      verify(cartLock).lock();
      verify(cartLock).unlock();
    }

    @Test
    void whenRemoveItem_removesFromSessionCart() {
      cartService.removeItem(1L);

      verify(sessionCart).removeItem(1L);
      verify(cartLock).lock();
      verify(cartLock).unlock();
    }

    @Test
    void whenGetCart_returnsSessionCartView() {
      when(product.getId()).thenReturn(1L);
      when(product.getPrice()).thenReturn(BigDecimal.valueOf(1500));

      ProductViewDto productDto = ProductViewDto.fromEntity(product, 2);
      CartSessionItem sessionItem = new CartSessionItem(productDto, 2);
      when(sessionCart.getItems()).thenReturn(List.of(sessionItem));
      when(productRepository.findAllById(List.of(product.getId()))).thenReturn(List.of(product));

      CartViewDto cartView = cartService.getCartForCurrentUser();

      assertThat(cartView.items()).hasSize(1);
      assertThat(cartView.items().stream().toList().get(0).product().id()).isEqualTo(1L);
      assertThat(cartView.items().stream().toList().get(0).product().inCartQuantity()).isEqualTo(2);
      assertThat(cartView.totalAmount()).isEqualByComparingTo("3000.00");
    }
  }
}
