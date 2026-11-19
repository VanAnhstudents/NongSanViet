package com.nsv.backend.dispute.repository;

import com.nsv.backend.dispute.entity.Complaint;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    boolean existsByOrderId(Long orderId);

    @EntityGraph(attributePaths = "evidences")
    Optional<Complaint> findWithEvidencesById(Long id);

    @EntityGraph(attributePaths = "evidences")
    Optional<Complaint> findWithEvidencesByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Complaint c WHERE c.id = :id")
    Optional<Complaint> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "evidences")
    List<Complaint> findAllByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    @EntityGraph(attributePaths = "evidences")
    List<Complaint> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "evidences")
    List<Complaint> findAllByStatusOrderByCreatedAtDesc(String status);
}
