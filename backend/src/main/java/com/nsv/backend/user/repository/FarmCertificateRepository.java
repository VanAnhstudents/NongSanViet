package com.nsv.backend.user.repository;

import com.nsv.backend.user.entity.FarmCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmCertificateRepository extends JpaRepository<FarmCertificate, Long> {
    List<FarmCertificate> findAllByOrderByCreatedAtDesc();
    List<FarmCertificate> findAllByStatusOrderByCreatedAtDesc(String status);
    List<FarmCertificate> findAllByFarmIdOrderByCreatedAtDesc(Long farmId);
    List<FarmCertificate> findAllByFarmIdAndStatusOrderByReviewedAtDesc(Long farmId, String status);
}
