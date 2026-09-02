package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findAllByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<Dish> findAllByCategoryIdAndAvailableTrue(Long categoryId);
}
