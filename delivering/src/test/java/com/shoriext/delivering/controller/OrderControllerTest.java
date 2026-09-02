package com.shoriext.delivering.controller;

import com.shoriext.delivering.entity.DeliveryStatus;
import com.shoriext.delivering.entity.OrderStatus;
import com.shoriext.delivering.service.OrderService;
import com.shoriext.delivering.service.dto.OrderDtos.CreateOrderCommand;
import com.shoriext.delivering.service.dto.OrderDtos.DeliveryResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderItemResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderStatisticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-02T10:00:00Z");

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
        CreateOrderCommand command = new CreateOrderCommand(3L, "Client street, 7");
        when(orderService.createOrder(command)).thenReturn(order(OrderStatus.NEW));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId": 3, "deliveryAddress": "Client street, 7"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items[0].dishName").value("Margherita"))
                .andExpect(jsonPath("$.delivery.deliveryAddress").value("Client street, 7"));

        verify(orderService).createOrder(command);
    }

    @Test
    void getOrderReturnsItemsAndDelivery() throws Exception {
        when(orderService.getOrder(20L)).thenReturn(order(OrderStatus.CONFIRMED));

        mockMvc.perform(get("/api/orders/{id}", 20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(3))
                .andExpect(jsonPath("$.restaurantId").value(1))
                .andExpect(jsonPath("$.items[0].amount").value(1300))
                .andExpect(jsonPath("$.delivery.status").value("WAITING_FOR_COURIER"));
    }

    @Test
    void findOrdersPassesAllFilters() throws Exception {
        when(orderService.findOrders(3L, 1L, OrderStatus.COMPLETED))
                .thenReturn(List.of(order(OrderStatus.COMPLETED)));

        mockMvc.perform(get("/api/orders")
                        .param("clientId", "3")
                        .param("restaurantId", "1")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(orderService).findOrders(3L, 1L, OrderStatus.COMPLETED);
    }

    @Test
    void updateStatusReturnsUpdatedOrder() throws Exception {
        when(orderService.updateStatus(20L, OrderStatus.CONFIRMED))
                .thenReturn(order(OrderStatus.CONFIRMED));

        mockMvc.perform(patch("/api/orders/{id}/status", 20)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CONFIRMED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService).updateStatus(20L, OrderStatus.CONFIRMED);
    }

    @Test
    void cancelOrderReturnsCancelledOrder() throws Exception {
        when(orderService.cancelOrder(20L)).thenReturn(order(OrderStatus.CANCELLED));

        mockMvc.perform(post("/api/orders/{id}/cancel", 20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService).cancelOrder(20L);
    }

    @Test
    void statisticsPassesRestaurantAndPeriod() throws Exception {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-09-30T23:59:59Z");
        when(orderService.getStatistics(1L, from, to))
                .thenReturn(new OrderStatisticsResponse(12, new BigDecimal("18500.00")));

        mockMvc.perform(get("/api/orders/statistics")
                        .param("restaurantId", "1")
                        .param("from", "2026-09-01T00:00:00Z")
                        .param("to", "2026-09-30T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(12))
                .andExpect(jsonPath("$.revenue").value(18500));

        verify(orderService).getStatistics(1L, from, to);
    }

    @Test
    void unsupportedOrderStatusReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private static OrderResponse order(OrderStatus status) {
        OrderItemResponse item = new OrderItemResponse(
                30L,
                10L,
                "Margherita",
                new BigDecimal("650.00"),
                2,
                new BigDecimal("1300.00")
        );
        DeliveryResponse delivery = new DeliveryResponse(
                40L,
                "Client street, 7",
                status == OrderStatus.CANCELLED ? DeliveryStatus.CANCELLED : DeliveryStatus.WAITING_FOR_COURIER,
                null,
                null
        );
        return new OrderResponse(
                20L,
                3L,
                1L,
                status,
                new BigDecimal("1300.00"),
                CREATED_AT,
                List.of(item),
                delivery
        );
    }
}
