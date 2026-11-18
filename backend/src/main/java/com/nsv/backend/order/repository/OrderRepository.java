package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Bản tối thiểu cho PBI-18. Nếu PBI đặt hàng đã có OrderRepository thì gộp vào bản đó. */
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndBuyerId(Long id, Long buyerId);
    @EntityGraph(attributePaths = {"items", "orderAddress"})
    Optional<Order> findWithDetailsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

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

    @Query("""
        SELECT DISTINCT p.farmerId FROM OrderItem oi
        JOIN Batch b ON oi.batchId = b.id
        JOIN Product p ON b.productId = p.id
        WHERE oi.order.id = :orderId
        """)
    List<Long> findDistinctFarmerIdsByOrderId(@Param("orderId") Long orderId);
}
