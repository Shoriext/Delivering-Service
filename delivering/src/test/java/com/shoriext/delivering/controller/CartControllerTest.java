package com.shoriext.delivering.controller;

import com.shoriext.delivering.service.CartService;
import com.shoriext.delivering.service.dto.CartDtos.AddCartItemCommand;
import com.shoriext.delivering.service.dto.CartDtos.CartItemResponse;
import com.shoriext.delivering.service.dto.CartDtos.CartResponse;
import com.shoriext.delivering.service.dto.CartDtos.UpdateCartItemCommand;
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
class CartControllerTest {

    @Mock
    private CartService cartService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CartController(cartService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void getCartReturnsCartAndTotal() throws Exception {
        when(cartService.getCart(3L)).thenReturn(cart());

        mockMvc.perform(get("/api/clients/{clientId}/cart", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(3))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(1300));
    }

    @Test
    void addItemReturnsUpdatedCart() throws Exception {
        AddCartItemCommand command = new AddCartItemCommand(10L, 2);
        when(cartService.addItem(3L, command)).thenReturn(cart());

        mockMvc.perform(post("/api/clients/{clientId}/cart/items", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dishId": 10, "quantity": 2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].dishId").value(10))
                .andExpect(jsonPath("$.totalAmount").value(1300));

        verify(cartService).addItem(3L, command);
    }

    @Test
    void updateItemReturnsUpdatedCart() throws Exception {
        UpdateCartItemCommand command = new UpdateCartItemCommand(2);
        when(cartService.updateItem(3L, 10L, command)).thenReturn(cart());

        mockMvc.perform(put("/api/clients/{clientId}/cart/items/{dishId}", 3, 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(cartService).updateItem(3L, 10L, command);
    }

    @Test
    void removeItemReturnsUpdatedCart() throws Exception {
        CartResponse emptyCart = new CartResponse(5L, 3L, null, List.of(), BigDecimal.ZERO);
        when(cartService.removeItem(3L, 10L)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/clients/{clientId}/cart/items/{dishId}", 3, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalAmount").value(0));

        verify(cartService).removeItem(3L, 10L);
    }

    private static CartResponse cart() {
        CartItemResponse item = new CartItemResponse(
                10L,
                "Margherita",
                new BigDecimal("650.00"),
                2,
                new BigDecimal("1300.00")
        );
        return new CartResponse(5L, 3L, 1L, List.of(item), new BigDecimal("1300.00"));
    }
}
