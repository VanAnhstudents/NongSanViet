package com.nsv.backend.dispute.repository;

import com.nsv.backend.dispute.entity.ComplaintEvidence;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintEvidenceRepository extends JpaRepository<ComplaintEvidence, Long> {

    @EntityGraph(attributePaths = "complaint")
    Optional<ComplaintEvidence> findWithComplaintById(Long id);

    List<ComplaintEvidence> findAllByComplaintIdOrderByCreatedAtAscIdAsc(Long complaintId);
}
