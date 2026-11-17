package com.nsv.backend.user.repository;

import com.nsv.backend.user.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Bản tối thiểu cho PBI-11. Nếu PBI-02 đã có FarmRepository thì xóa file này.
 */
public interface FarmRepository extends JpaRepository<Farm, Long> {

    Optional<Farm> findByUserId(Long userId);
}
