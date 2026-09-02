package com.shoriext.delivering.service;

import com.shoriext.delivering.entity.Cart;
import com.shoriext.delivering.entity.CartItem;
import com.shoriext.delivering.entity.Client;
import com.shoriext.delivering.entity.Dish;
import com.shoriext.delivering.repository.CartItemRepository;
import com.shoriext.delivering.repository.CartRepository;
import com.shoriext.delivering.repository.ClientRepository;
import com.shoriext.delivering.repository.DishRepository;
import com.shoriext.delivering.service.dto.CartDtos.AddCartItemCommand;
import com.shoriext.delivering.service.dto.CartDtos.CartItemResponse;
import com.shoriext.delivering.service.dto.CartDtos.CartResponse;
import com.shoriext.delivering.service.dto.CartDtos.UpdateCartItemCommand;
import com.shoriext.delivering.service.exception.BusinessRuleException;
import com.shoriext.delivering.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final DishRepository dishRepository;

    @Transactional
    public CartResponse getCart(Long clientId) {
        return toResponse(getOrCreateCart(clientId));
    }

    @Transactional
    public CartResponse addItem(Long clientId, AddCartItemCommand command) {
        requirePositive(command.quantity());
        Dish dish = dishRepository.findById(command.dishId())
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + command.dishId()));
        if (!dish.isAvailable() || !dish.getRestaurant().isActive()) {
            throw new BusinessRuleException("Dish is not available for ordering");
        }

        Cart cart = getOrCreateCart(clientId);
        ensureSameRestaurant(cart, dish);
        CartItem item = cartItemRepository.findByCartIdAndDishId(cart.getId(), dish.getId())
                .orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setDish(dish);
            item.setQuantity(command.quantity());
            cart.addItem(item);
        } else {
            item.setQuantity(item.getQuantity() + command.quantity());
        }
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Long clientId, Long dishId, UpdateCartItemCommand command) {
        requirePositive(command.quantity());
        Cart cart = findCart(clientId);
        CartItem item = cartItemRepository.findByCartIdAndDishId(cart.getId(), dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish is not present in the cart: " + dishId));
        item.setQuantity(command.quantity());
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long clientId, Long dishId) {
        Cart cart = findCart(clientId);
        CartItem item = cartItemRepository.findByCartIdAndDishId(cart.getId(), dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish is not present in the cart: " + dishId));
        cart.removeItem(item);
        return toResponse(cart);
    }

    private Cart getOrCreateCart(Long clientId) {
        return cartRepository.findByClientId(clientId).orElseGet(() -> {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + clientId));
            Cart cart = new Cart();
            cart.setClient(client);
            client.setCart(cart);
            return cartRepository.save(cart);
        });
    }

    private Cart findCart(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found: " + clientId);
        }
        return cartRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for client: " + clientId));
    }

    private static void ensureSameRestaurant(Cart cart, Dish dish) {
        boolean anotherRestaurant = cart.getItems().stream()
                .anyMatch(item -> !item.getDish().getRestaurant().getId().equals(dish.getRestaurant().getId()));
        if (anotherRestaurant) {
            throw new BusinessRuleException("A cart can contain dishes from only one restaurant");
        }
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal amount = item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemResponse(
                            item.getDish().getId(),
                            item.getDish().getName(),
                            item.getDish().getPrice(),
                            item.getQuantity(),
                            amount
                    );
                })
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Long restaurantId = cart.getItems().isEmpty()
                ? null
                : cart.getItems().get(0).getDish().getRestaurant().getId();
        return new CartResponse(cart.getId(), cart.getClient().getId(), restaurantId, items, total);
    }
}
