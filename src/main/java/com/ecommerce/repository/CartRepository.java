package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
  Optional<Cart> findByUser(User user);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user = :user")
  Optional<Cart> findByUserWithLock(User user);
}
