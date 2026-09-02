package com.shoriext.delivering.controller;

import com.shoriext.delivering.service.DishService;
import com.shoriext.delivering.service.dto.DishDtos.CreateDishCommand;
import com.shoriext.delivering.service.dto.DishDtos.DishResponse;
import com.shoriext.delivering.service.dto.DishDtos.UpdateDishCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DishControllerTest {

    @Mock
    private DishService dishService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DishController(dishService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void findRestaurantDishesPassesAllFilters() throws Exception {
        when(dishService.findRestaurantDishes(
                1L, 2L, new BigDecimal("100"), new BigDecimal("900"), true
        )).thenReturn(List.of(dish()));

        mockMvc.perform(get("/api/restaurants/{restaurantId}/dishes", 1)
                        .param("categoryId", "2")
                        .param("minPrice", "100")
                        .param("maxPrice", "900")
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Margherita"))
                .andExpect(jsonPath("$[0].categoryId").value(2));

        verify(dishService).findRestaurantDishes(
                1L, 2L, new BigDecimal("100"), new BigDecimal("900"), true
        );
    }

    @Test
    void createDishReturnsCreated() throws Exception {
        CreateDishCommand command = new CreateDishCommand(
                "Margherita", "Classic pizza", new BigDecimal("650.00"), 2L
        );
        when(dishService.createDish(1L, command)).thenReturn(dish());

        mockMvc.perform(post("/api/restaurants/{restaurantId}/dishes", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Margherita",
                                  "description": "Classic pizza",
                                  "price": 650.00,
                                  "categoryId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.restaurantId").value(1));

        verify(dishService).createDish(1L, command);
    }

    @Test
    void updateDishReturnsUpdatedDish() throws Exception {
        UpdateDishCommand command = new UpdateDishCommand(
                "Margherita", "Updated", new BigDecimal("700.00"), 2L, false
        );
        DishResponse updated = new DishResponse(
                10L, "Margherita", "Updated", new BigDecimal("700.00"),
                2L, "Pizza", 1L, false
        );
        when(dishService.updateDish(10L, command)).thenReturn(updated);

        mockMvc.perform(put("/api/dishes/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Margherita",
                                  "description": "Updated",
                                  "price": 700.00,
                                  "categoryId": 2,
                                  "available": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(700))
                .andExpect(jsonPath("$.available").value(false));

        verify(dishService).updateDish(10L, command);
    }

    @Test
    void deleteDishReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/dishes/{id}", 10))
                .andExpect(status().isNoContent());

        verify(dishService).deleteDish(10L);
    }

    private static DishResponse dish() {
        return new DishResponse(
                10L,
                "Margherita",
                "Classic pizza",
                new BigDecimal("650.00"),
                2L,
                "Pizza",
                1L,
                true
        );
    }
}
