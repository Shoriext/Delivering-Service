package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long>, JpaSpecificationExecutor<Dish> {

    List<Dish> findAllByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<Dish> findAllByCategoryIdAndAvailableTrue(Long categoryId);
}
