package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/** Bản tối thiểu cho PBI-18. Nếu PBI đặt hàng đã có OrderItemRepository thì gộp vào bản đó. */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select coalesce(sum(i.quantity * i.unitPrice), 0) from OrderItem i where i.orderId = :orderId")
    BigDecimal sumAmountByOrderId(@Param("orderId") Long orderId);
}
