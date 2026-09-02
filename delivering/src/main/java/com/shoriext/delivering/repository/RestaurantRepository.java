package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {

    List<Restaurant> findAllByActiveTrue();

    List<Restaurant> findAllByNameContainingIgnoreCase(String name);
}
