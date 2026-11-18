package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    List<Payment> findAllByOrderIdOrderByCreatedAtAsc(Long orderId);

    Optional<Payment> findFirstByOrderIdOrderByIdDesc(Long orderId);

    boolean existsByProviderRef(String providerRef);
}
