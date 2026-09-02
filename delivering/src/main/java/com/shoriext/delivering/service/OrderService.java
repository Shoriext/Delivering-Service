package com.shoriext.delivering.service;

import com.shoriext.delivering.entity.Cart;
import com.shoriext.delivering.entity.CartItem;
import com.shoriext.delivering.entity.Client;
import com.shoriext.delivering.entity.Delivery;
import com.shoriext.delivering.entity.DeliveryStatus;
import com.shoriext.delivering.entity.FoodOrder;
import com.shoriext.delivering.entity.OrderItem;
import com.shoriext.delivering.entity.OrderStatus;
import com.shoriext.delivering.entity.Restaurant;
import com.shoriext.delivering.repository.CartRepository;
import com.shoriext.delivering.repository.ClientRepository;
import com.shoriext.delivering.repository.FoodOrderRepository;
import com.shoriext.delivering.service.dto.OrderDtos.CreateOrderCommand;
import com.shoriext.delivering.service.dto.OrderDtos.DeliveryResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderItemResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderResponse;
import com.shoriext.delivering.service.dto.OrderDtos.OrderStatisticsResponse;
import com.shoriext.delivering.service.exception.BusinessRuleException;
import com.shoriext.delivering.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.NEW, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.COOKING, OrderStatus.CANCELLED),
            OrderStatus.COOKING, EnumSet.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.READY_FOR_DELIVERY, EnumSet.of(OrderStatus.IN_DELIVERY, OrderStatus.CANCELLED),
            OrderStatus.IN_DELIVERY, EnumSet.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class)
    );

    private final FoodOrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final CartRepository cartRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderCommand command) {
        if (command.clientId() == null) {
            throw new IllegalArgumentException("clientId must not be null");
        }
        if (command.deliveryAddress() == null || command.deliveryAddress().isBlank()) {
            throw new IllegalArgumentException("Delivery address must not be blank");
        }

        Client client = clientRepository.findById(command.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + command.clientId()));
        Cart cart = cartRepository.findByClientId(command.clientId())
                .orElseThrow(() -> new BusinessRuleException("Client cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Client cart is empty");
        }

        Restaurant restaurant = cart.getItems().get(0).getDish().getRestaurant();
        validateCart(cart, restaurant);
        BigDecimal total = calculateTotal(cart.getItems());
        if (total.compareTo(restaurant.getMinimumOrderAmount()) < 0) {
            throw new BusinessRuleException("Minimum order amount is " + restaurant.getMinimumOrderAmount());
        }

        FoodOrder order = new FoodOrder();
        order.setClient(client);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(total);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setDish(cartItem.getDish());
            item.setDishName(cartItem.getDish().getName());
            item.setUnitPrice(cartItem.getDish().getPrice());
            item.setQuantity(cartItem.getQuantity());
            order.addItem(item);
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryAddress(command.deliveryAddress().trim());
        delivery.setStatus(DeliveryStatus.WAITING_FOR_COURIER);
        order.setDelivery(delivery);

        FoodOrder savedOrder = orderRepository.save(order);
        for (CartItem cartItem : new ArrayList<>(cart.getItems())) {
            cart.removeItem(cartItem);
        }
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findOrders(Long clientId, Long restaurantId, OrderStatus status) {
        Specification<FoodOrder> specification = orderSpecification(clientId, restaurantId, status, null, null);
        return orderRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(OrderService::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status must not be null");
        }
        FoodOrder order = findOrder(id);
        changeStatus(order, newStatus);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        FoodOrder order = findOrder(id);
        changeStatus(order, OrderStatus.CANCELLED);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderStatisticsResponse getStatistics(Long restaurantId, Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("The beginning of the period must not be after its end");
        }
        Specification<FoodOrder> specification = orderSpecification(null, restaurantId, null, from, to);
        List<FoodOrder> orders = orderRepository.findAll(specification);
        BigDecimal revenue = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(FoodOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderStatisticsResponse(orders.size(), revenue);
    }

    private static Specification<FoodOrder> orderSpecification(
            Long clientId,
            Long restaurantId,
            OrderStatus status,
            Instant from,
            Instant to
    ) {
        Specification<FoodOrder> specification = (root, query, cb) -> cb.conjunction();
        if (clientId != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("client").get("id"), clientId)
            );
        }
        if (restaurantId != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("restaurant").get("id"), restaurantId)
            );
        }
        if (status != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("status"), status)
            );
        }
        if (from != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from)
            );
        }
        if (to != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to)
            );
        }
        return specification;
    }

    private static void validateCart(Cart cart, Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new BusinessRuleException("Restaurant is not accepting orders");
        }
        for (CartItem item : cart.getItems()) {
            if (!item.getDish().isAvailable()) {
                throw new BusinessRuleException("Dish is no longer available: " + item.getDish().getName());
            }
            if (!item.getDish().getRestaurant().getId().equals(restaurant.getId())) {
                throw new BusinessRuleException("A cart can contain dishes from only one restaurant");
            }
        }
    }

    private static BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getDish().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FoodOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private static void changeStatus(FoodOrder order, OrderStatus newStatus) {
        if (!ALLOWED_TRANSITIONS.get(order.getStatus()).contains(newStatus)) {
            throw new BusinessRuleException(
                    "Order cannot transition from " + order.getStatus() + " to " + newStatus
            );
        }
        order.setStatus(newStatus);
        Delivery delivery = order.getDelivery();
        if (newStatus == OrderStatus.IN_DELIVERY) {
            delivery.setStatus(DeliveryStatus.ON_THE_WAY);
        } else if (newStatus == OrderStatus.COMPLETED) {
            delivery.setStatus(DeliveryStatus.DELIVERED);
            delivery.setDeliveredAt(Instant.now());
        } else if (newStatus == OrderStatus.CANCELLED) {
            delivery.setStatus(DeliveryStatus.CANCELLED);
        }
    }

    private static OrderResponse toResponse(FoodOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getDish() == null ? null : item.getDish().getId(),
                        item.getDishName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        Delivery delivery = order.getDelivery();
        DeliveryResponse deliveryResponse = delivery == null ? null : new DeliveryResponse(
                delivery.getId(),
                delivery.getDeliveryAddress(),
                delivery.getStatus(),
                delivery.getEstimatedDeliveryAt(),
                delivery.getDeliveredAt()
        );
        return new OrderResponse(
                order.getId(),
                order.getClient().getId(),
                order.getRestaurant().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items,
                deliveryResponse
        );
    }
}
