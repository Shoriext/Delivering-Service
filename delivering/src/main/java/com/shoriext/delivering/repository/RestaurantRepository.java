package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findAllByActiveTrue();

    List<Restaurant> findAllByNameContainingIgnoreCase(String name);
}
