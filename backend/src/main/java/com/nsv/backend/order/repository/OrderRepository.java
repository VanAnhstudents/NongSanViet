package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "orderAddress"})
    Optional<Order> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"items", "orderAddress"})
    Optional<Order> findWithDetailsByIdAndBuyerId(Long id, Long buyerId);

    @EntityGraph(attributePaths = {"items", "orderAddress"})
    List<Order> findAllByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    @EntityGraph(attributePaths = {"items", "orderAddress"})
    @Query("""
        SELECT DISTINCT o FROM Order o
        JOIN o.items oi
        JOIN Batch b ON oi.batchId = b.id
        JOIN Product p ON b.productId = p.id
        WHERE p.farmerId = :farmerId
          AND (:status IS NULL OR o.status = :status)
        ORDER BY o.createdAt DESC
        """)
    List<Order> findAllByFarmerIdAndOptionalStatus(
        @Param("farmerId") Long farmerId,
        @Param("status") String status
    );

    @Query("""
        SELECT COUNT(oi.id) > 0 FROM OrderItem oi
        JOIN Batch b ON oi.batchId = b.id
        JOIN Product p ON b.productId = p.id
        WHERE oi.order.id = :orderId
          AND p.farmerId = :farmerId
        """)
    boolean existsOrderForFarmer(
        @Param("orderId") Long orderId,
        @Param("farmerId") Long farmerId
    );
}
