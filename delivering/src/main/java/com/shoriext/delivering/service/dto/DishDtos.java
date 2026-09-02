package com.shoriext.delivering.service.dto;

import java.math.BigDecimal;

public final class DishDtos {

    private DishDtos() {
    }

    public record CreateDishCommand(
            String name,
            String description,
            BigDecimal price,
            Long categoryId
    ) {
    }

    public record UpdateDishCommand(
            String name,
            String description,
            BigDecimal price,
            Long categoryId,
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
