package com.shoriext.delivering.service.dto;

import com.shoriext.delivering.entity.DeliveryStatus;
import com.shoriext.delivering.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderCommand(Long clientId, String deliveryAddress) {
    }

    public record OrderItemResponse(
            Long id,
            Long dishId,
            String dishName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal amount
    ) {
    }

    public record DeliveryResponse(
            Long id,
            String deliveryAddress,
            DeliveryStatus status,
            Instant estimatedDeliveryAt,
            Instant deliveredAt
    ) {
    }

    public record OrderResponse(
            Long id,
            Long clientId,
            Long restaurantId,
            OrderStatus status,
            BigDecimal totalAmount,
            Instant createdAt,
            List<OrderItemResponse> items,
            DeliveryResponse delivery
    ) {
    }

    public record OrderStatisticsResponse(long orderCount, BigDecimal revenue) {
    }
}
