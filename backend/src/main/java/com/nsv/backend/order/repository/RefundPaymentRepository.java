package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Payment;
import com.nsv.backend.order.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Truy vấn giao dịch thanh toán phục vụ hoàn tiền (bảng payments). */
public interface RefundPaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
