package com.shoriext.delivering.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddCartItemCommand(@NotNull Long dishId, @Positive int quantity) {
    }

    public record UpdateCartItemCommand(@Positive int quantity) {
    }

    public record CartItemResponse(
            Long dishId,
            String dishName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal amount
    ) {
    }

    public record CartResponse(
            Long id,
            Long clientId,
            Long restaurantId,
            List<CartItemResponse> items,
            BigDecimal totalAmount
    ) {
    }
}
