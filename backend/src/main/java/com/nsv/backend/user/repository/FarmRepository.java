package com.nsv.backend.user.repository;

import com.nsv.backend.user.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    boolean existsByUserId(Long userId);
    Optional<Farm> findByUserId(Long userId);
}
