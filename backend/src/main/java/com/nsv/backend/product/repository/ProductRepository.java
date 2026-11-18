package com.nsv.backend.product.repository;

import com.nsv.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdInAndFarmerId(Collection<Long> ids, Long farmerId);
    List<Product> findAllByIdIn(Collection<Long> ids);
    Optional<Product> findByIdAndFarmerId(Long id, Long farmerId);
}
