package com.shoriext.delivering.service.dto;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddCartItemCommand(Long dishId, int quantity) {
    }

    public record UpdateCartItemCommand(int quantity) {
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
