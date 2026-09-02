package com.shoriext.delivering.controller;

import com.shoriext.delivering.entity.OrderStatus;
import com.shoriext.delivering.service.OrderService;
import com.shoriext.delivering.service.dto.OrderDtos.CreateOrderCommand;
import com.shoriext.delivering.service.dto.OrderDtos.OrderResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderStatisticsResponse;
import com.shoriext.delivering.service.dto.OrderDtos.UpdateOrderStatusCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderCommand command) {
        return orderService.createOrder(command);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    public List<OrderResponse> findOrders(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) OrderStatus status
    ) {
        return orderService.findOrders(clientId, restaurantId, status);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusCommand command
    ) {
        return orderService.updateStatus(id, command.status());
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/statistics")
    public OrderStatisticsResponse getStatistics(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return orderService.getStatistics(restaurantId, from, to);
    }
}
