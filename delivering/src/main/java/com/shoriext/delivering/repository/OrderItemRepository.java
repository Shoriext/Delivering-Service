package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findAllByOrderId(Long orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OrderItem item set item.dish = null where item.dish.id = :dishId")
    void detachDish(@Param("dishId") Long dishId);
}
