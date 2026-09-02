package com.shoriext.delivering.service;

import com.shoriext.delivering.entity.Restaurant;
import com.shoriext.delivering.repository.RestaurantRepository;
import com.shoriext.delivering.service.dto.RestaurantDtos.CreateRestaurantCommand;
import com.shoriext.delivering.service.dto.RestaurantDtos.RestaurantResponse;
import com.shoriext.delivering.service.dto.RestaurantDtos.UpdateRestaurantCommand;
import com.shoriext.delivering.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<RestaurantResponse> findRestaurants(String name, Boolean active) {
        Specification<Restaurant> specification = (root, query, cb) -> cb.conjunction();

        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and(
                    (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern)
            );
        }
        if (active != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("active"), active)
            );
        }

        return restaurantRepository.findAll(specification).stream()
                .map(RestaurantService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(Long id) {
        return toResponse(findRestaurant(id));
    }

    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantCommand command) {
        requireText(command.name(), "Restaurant name");
        requireText(command.address(), "Restaurant address");
        requireNonNegative(command.minimumOrderAmount(), "Minimum order amount");

        Restaurant restaurant = new Restaurant();
        restaurant.setName(command.name().trim());
        restaurant.setAddress(command.address().trim());
        restaurant.setMinimumOrderAmount(command.minimumOrderAmount());
        restaurant.setActive(true);
        return toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, UpdateRestaurantCommand command) {
        requireText(command.name(), "Restaurant name");
        requireText(command.address(), "Restaurant address");
        requireNonNegative(command.minimumOrderAmount(), "Minimum order amount");

        Restaurant restaurant = findRestaurant(id);
        restaurant.setName(command.name().trim());
        restaurant.setAddress(command.address().trim());
        restaurant.setMinimumOrderAmount(command.minimumOrderAmount());
        restaurant.setActive(command.active());
        return toResponse(restaurant);
    }

    private Restaurant findRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    private static RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.isActive(),
                restaurant.getMinimumOrderAmount()
        );
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be zero or greater");
        }
    }
}
