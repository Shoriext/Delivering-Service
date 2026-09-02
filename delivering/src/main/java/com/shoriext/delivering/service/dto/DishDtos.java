package com.shoriext.delivering.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class DishDtos {

    private DishDtos() {
    }

    public record CreateDishCommand(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin(value = "0.01") BigDecimal price,
            @NotNull Long categoryId
    ) {
    }

    public record UpdateDishCommand(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin(value = "0.01") BigDecimal price,
            @NotNull Long categoryId,
            boolean available
    ) {
    }

    public record DishResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Long categoryId,
            String categoryName,
            Long restaurantId,
            boolean available
    ) {
    }
}
