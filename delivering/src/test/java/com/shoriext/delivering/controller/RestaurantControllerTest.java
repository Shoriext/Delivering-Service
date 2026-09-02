package com.shoriext.delivering.controller;

import com.shoriext.delivering.service.RestaurantService;
import com.shoriext.delivering.service.dto.RestaurantDtos.CreateRestaurantCommand;
import com.shoriext.delivering.service.dto.RestaurantDtos.RestaurantResponse;
import com.shoriext.delivering.service.dto.RestaurantDtos.UpdateRestaurantCommand;
import com.shoriext.delivering.service.exception.ResourceNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private RestaurantService restaurantService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RestaurantController(restaurantService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void findRestaurantsReturnsFilteredRestaurants() throws Exception {
        when(restaurantService.findRestaurants("pizza", true)).thenReturn(List.of(restaurant()));

        mockMvc.perform(get("/api/restaurants")
                        .param("name", "pizza")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Pizza Place"))
                .andExpect(jsonPath("$[0].minimumOrderAmount").value(500));

        verify(restaurantService).findRestaurants("pizza", true);
    }

    @Test
    void getRestaurantReturnsRestaurant() throws Exception {
        when(restaurantService.getRestaurant(1L)).thenReturn(restaurant());

        mockMvc.perform(get("/api/restaurants/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.address").value("Main street, 1"));
    }

    @Test
    void createRestaurantReturnsCreated() throws Exception {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Pizza Place", "Main street, 1", new BigDecimal("500.00")
        );
        when(restaurantService.createRestaurant(command)).thenReturn(restaurant());

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Pizza Place",
                                  "address": "Main street, 1",
                                  "minimumOrderAmount": 500.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(true));

        verify(restaurantService).createRestaurant(command);
    }

    @Test
    void updateRestaurantReturnsUpdatedRestaurant() throws Exception {
        UpdateRestaurantCommand command = new UpdateRestaurantCommand(
                "New name", "New address", new BigDecimal("700.00"), false
        );
        RestaurantResponse updated = new RestaurantResponse(
                1L, "New name", "New address", false, new BigDecimal("700.00")
        );
        when(restaurantService.updateRestaurant(1L, command)).thenReturn(updated);

        mockMvc.perform(put("/api/restaurants/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New name",
                                  "address": "New address",
                                  "minimumOrderAmount": 700.00,
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.active").value(false));

        verify(restaurantService).updateRestaurant(1L, command);
    }

    @Test
    void missingRestaurantReturnsNotFoundError() throws Exception {
        when(restaurantService.getRestaurant(99L))
                .thenThrow(new ResourceNotFoundException("Restaurant not found: 99"));

        mockMvc.perform(get("/api/restaurants/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Restaurant not found: 99"))
                .andExpect(jsonPath("$.path").value("/api/restaurants/99"));
    }

    @Test
    void invalidCreateRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "address": "",
                                  "minimumOrderAmount": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.address").exists())
                .andExpect(jsonPath("$.fieldErrors.minimumOrderAmount").exists());
    }

    private static RestaurantResponse restaurant() {
        return new RestaurantResponse(
                1L,
                "Pizza Place",
                "Main street, 1",
                true,
                new BigDecimal("500.00")
        );
    }
}
