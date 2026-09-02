package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndDishId(Long cartId, Long dishId);

    void deleteAllByDishId(Long dishId);
}
