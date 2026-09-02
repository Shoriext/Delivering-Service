package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByClientId(Long clientId);
}
