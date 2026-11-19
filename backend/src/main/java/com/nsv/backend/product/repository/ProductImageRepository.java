package com.nsv.backend.product.repository;

import com.nsv.backend.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findAllByProductIdOrderBySortOrderAscIdAsc(Long productId);
    List<ProductImage> findAllByProductIdInOrderBySortOrderAscIdAsc(Collection<Long> productIds);
}
