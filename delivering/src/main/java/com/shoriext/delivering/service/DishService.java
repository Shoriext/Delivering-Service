package com.shoriext.delivering.service;

import com.shoriext.delivering.entity.Dish;
import com.shoriext.delivering.entity.MenuCategory;
import com.shoriext.delivering.entity.Restaurant;
import com.shoriext.delivering.repository.CartItemRepository;
import com.shoriext.delivering.repository.DishRepository;
import com.shoriext.delivering.repository.MenuCategoryRepository;
import com.shoriext.delivering.repository.OrderItemRepository;
import com.shoriext.delivering.repository.RestaurantRepository;
import com.shoriext.delivering.service.dto.DishDtos.CreateDishCommand;
import com.shoriext.delivering.service.dto.DishDtos.DishResponse;
import com.shoriext.delivering.service.dto.DishDtos.UpdateDishCommand;
import com.shoriext.delivering.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<DishResponse> findRestaurantDishes(
            Long restaurantId,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean available
    ) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        }
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("Price filters must be zero or greater");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must not exceed maxPrice");
        }

        Specification<Dish> specification = (root, query, cb) ->
                cb.equal(root.get("restaurant").get("id"), restaurantId);
        if (categoryId != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId)
            );
        }
        if (minPrice != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice)
            );
        }
        if (maxPrice != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice)
            );
        }
        if (available != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("available"), available)
            );
        }

        return dishRepository.findAll(specification).stream()
                .map(DishService::toResponse)
                .toList();
    }

    @Transactional
    public DishResponse createDish(Long restaurantId, CreateDishCommand command) {
        validate(command.name(), command.price(), command.categoryId());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        MenuCategory category = findCategory(command.categoryId());

        Dish dish = new Dish();
        dish.setName(command.name().trim());
        dish.setDescription(command.description());
        dish.setPrice(command.price());
        dish.setRestaurant(restaurant);
        dish.setCategory(category);
        dish.setAvailable(true);
        return toResponse(dishRepository.save(dish));
    }

    @Transactional
    public DishResponse updateDish(Long id, UpdateDishCommand command) {
        validate(command.name(), command.price(), command.categoryId());
        Dish dish = findDish(id);
        dish.setName(command.name().trim());
        dish.setDescription(command.description());
        dish.setPrice(command.price());
        dish.setCategory(findCategory(command.categoryId()));
        dish.setAvailable(command.available());
        return toResponse(dish);
    }

    @Transactional
    public void deleteDish(Long id) {
        Dish dish = findDish(id);
        cartItemRepository.deleteAllByDishId(id);
        orderItemRepository.detachDish(id);
        dishRepository.delete(dish);
    }

    private Dish findDish(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + id));
    }

    private MenuCategory findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found: " + id));
    }

    private static void validate(String name, BigDecimal price, Long categoryId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Dish name must not be blank");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Dish price must be greater than zero");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
    }

    private static DishResponse toResponse(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getCategory().getId(),
                dish.getCategory().getName(),
                dish.getRestaurant().getId(),
                dish.isAvailable()
        );
    }
}
