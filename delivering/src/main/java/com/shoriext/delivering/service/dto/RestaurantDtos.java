package com.shoriext.delivering.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class RestaurantDtos {

    private RestaurantDtos() {
    }

    public record CreateRestaurantCommand(
            @NotBlank String name,
            @NotBlank String address,
            @NotNull @DecimalMin("0.00") BigDecimal minimumOrderAmount
    ) {
    }

    public record UpdateRestaurantCommand(
            @NotBlank String name,
            @NotBlank String address,
            @NotNull @DecimalMin("0.00") BigDecimal minimumOrderAmount,
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
