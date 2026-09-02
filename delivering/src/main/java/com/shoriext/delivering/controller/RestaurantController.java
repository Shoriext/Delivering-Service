package com.shoriext.delivering.controller;

import com.shoriext.delivering.service.RestaurantService;
import com.shoriext.delivering.service.dto.RestaurantDtos.CreateRestaurantCommand;
import com.shoriext.delivering.service.dto.RestaurantDtos.RestaurantResponse;
import com.shoriext.delivering.service.dto.RestaurantDtos.UpdateRestaurantCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantResponse> findRestaurants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active
    ) {
        return restaurantService.findRestaurants(name, active);
    }

    @GetMapping("/{id}")
    public RestaurantResponse getRestaurant(@PathVariable Long id) {
        return restaurantService.getRestaurant(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody CreateRestaurantCommand command) {
        return restaurantService.createRestaurant(command);
    }

    @PutMapping("/{id}")
    public RestaurantResponse updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantCommand command
    ) {
        return restaurantService.updateRestaurant(id, command);
    }
}
