package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "orderAddress"})
    Optional<Order> findWithDetailsByIdAndBuyerId(Long id, Long buyerId);
}
