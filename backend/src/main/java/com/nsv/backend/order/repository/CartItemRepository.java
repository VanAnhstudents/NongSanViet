package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUserIdOrderByIdAsc(Long userId);
    List<CartItem> findAllByUserIdAndIdInOrderByIdAsc(Long userId, Collection<Long> ids);
    Optional<CartItem> findByUserIdAndBatchId(Long userId, Long batchId);
    Optional<CartItem> findByIdAndUserId(Long id, Long userId);
}
