package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Bản tối thiểu cho PBI-18. Nếu PBI đặt hàng đã có OrderRepository thì gộp vào bản đó. */
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndBuyerId(Long id, Long buyerId);
}
