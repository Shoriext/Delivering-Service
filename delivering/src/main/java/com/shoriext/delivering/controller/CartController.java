package com.shoriext.delivering.controller;

import com.shoriext.delivering.service.CartService;
import com.shoriext.delivering.service.dto.CartDtos.AddCartItemCommand;
import com.shoriext.delivering.service.dto.CartDtos.CartResponse;
import com.shoriext.delivering.service.dto.CartDtos.UpdateCartItemCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients/{clientId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@PathVariable Long clientId) {
        return cartService.getCart(clientId);
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @PathVariable Long clientId,
            @Valid @RequestBody AddCartItemCommand command
    ) {
        return cartService.addItem(clientId, command);
    }

    @PutMapping("/items/{dishId}")
    public CartResponse updateItem(
            @PathVariable Long clientId,
            @PathVariable Long dishId,
            @Valid @RequestBody UpdateCartItemCommand command
    ) {
        return cartService.updateItem(clientId, dishId, command);
    }

    @DeleteMapping("/items/{dishId}")
    public CartResponse removeItem(@PathVariable Long clientId, @PathVariable Long dishId) {
        return cartService.removeItem(clientId, dishId);
    }
}
