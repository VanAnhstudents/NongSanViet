package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findAllByPaymentIdOrderByCreatedAtAsc(Long paymentId);
    List<Refund> findAllByPaymentIdInOrderByCreatedAtAsc(Collection<Long> paymentIds);
}
