package com.shoriext.delivering.controller;

import com.shoriext.delivering.service.DishService;
import com.shoriext.delivering.service.dto.DishDtos.CreateDishCommand;
import com.shoriext.delivering.service.dto.DishDtos.DishResponse;
import com.shoriext.delivering.service.dto.DishDtos.UpdateDishCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping("/api/restaurants/{restaurantId}/dishes")
    public List<DishResponse> findRestaurantDishes(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available
    ) {
        return dishService.findRestaurantDishes(restaurantId, categoryId, minPrice, maxPrice, available);
    }

    @PostMapping("/api/restaurants/{restaurantId}/dishes")
    @ResponseStatus(HttpStatus.CREATED)
    public DishResponse createDish(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateDishCommand command
    ) {
        return dishService.createDish(restaurantId, command);
    }

    @PutMapping("/api/dishes/{id}")
    public DishResponse updateDish(@PathVariable Long id, @Valid @RequestBody UpdateDishCommand command) {
        return dishService.updateDish(id, command);
    }

    @DeleteMapping("/api/dishes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
    }
}
