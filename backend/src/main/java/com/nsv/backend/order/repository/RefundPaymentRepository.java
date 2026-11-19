package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundPaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderIdAndStatus(Long orderId, String status);
}
