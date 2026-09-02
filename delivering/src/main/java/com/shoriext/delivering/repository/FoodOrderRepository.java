package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.FoodOrder;
import com.shoriext.delivering.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

    List<FoodOrder> findAllByClientIdOrderByCreatedAtDesc(Long clientId);

    List<FoodOrder> findAllByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
}
