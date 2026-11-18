package com.nsv.backend.product.repository;

import com.nsv.backend.product.entity.Combo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComboRepository extends JpaRepository<Combo, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Combo> findWithItemsById(Long id);

    @EntityGraph(attributePaths = "items")
    List<Combo> findAllByStatusOrderByCreatedAtDesc(String status);
}
