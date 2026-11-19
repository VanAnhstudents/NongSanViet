package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.OrderCoInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderCoInspectionRepository extends JpaRepository<OrderCoInspection, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<OrderCoInspection> findByOrderId(Long orderId);

    @Query("""
        SELECT DISTINCT ci FROM OrderCoInspection ci
        JOIN OrderItem oi ON oi.order.id = ci.orderId
        JOIN Batch b ON oi.batchId = b.id
        JOIN Product p ON b.productId = p.id
        WHERE p.farmerId = :farmerId
        ORDER BY ci.createdAt DESC
        """)
    List<OrderCoInspection> findAllByFarmerId(@Param("farmerId") Long farmerId);
}
