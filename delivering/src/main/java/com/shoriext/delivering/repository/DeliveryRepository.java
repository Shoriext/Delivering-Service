package com.shoriext.delivering.repository;

import com.shoriext.delivering.entity.Delivery;
import com.shoriext.delivering.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findAllByStatus(DeliveryStatus status);
}
