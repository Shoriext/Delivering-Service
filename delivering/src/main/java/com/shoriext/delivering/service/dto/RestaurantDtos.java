package com.shoriext.delivering.service.dto;

import java.math.BigDecimal;

public final class RestaurantDtos {

    private RestaurantDtos() {
    }

    public record CreateRestaurantCommand(
            String name,
            String address,
            BigDecimal minimumOrderAmount
    ) {
    }

    public record UpdateRestaurantCommand(
            String name,
            String address,
            BigDecimal minimumOrderAmount,
            boolean active
    ) {
    }

    public record RestaurantResponse(
            Long id,
            String name,
            String address,
            boolean active,
            BigDecimal minimumOrderAmount
    ) {
    }
}
