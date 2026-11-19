package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
