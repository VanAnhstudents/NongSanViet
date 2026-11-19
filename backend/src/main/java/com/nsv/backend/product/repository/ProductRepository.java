package com.nsv.backend.product.repository;

import com.nsv.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdInAndFarmerId(Collection<Long> ids, Long farmerId);
    List<Product> findAllByIdIn(Collection<Long> ids);
    Optional<Product> findByIdAndFarmerId(Long id, Long farmerId);

    List<Product> findAllByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN Batch b ON b.productId = p.id
        JOIN Farm f ON f.userId = p.farmerId
        WHERE b.status IN ('AVAILABLE', 'PRE_ORDER')
          AND b.remainingQuantity > 0
          AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.categoryId = :categoryId)
          AND (:provinceId IS NULL OR f.provinceId = :provinceId)
          AND (:minPrice IS NULL OR b.unitPrice >= :minPrice)
          AND (:maxPrice IS NULL OR b.unitPrice <= :maxPrice)
        ORDER BY p.createdAt DESC, p.id DESC
        """)
    List<Product> searchPublicProducts(
        @Param("keyword") String keyword,
        @Param("categoryId") Long categoryId,
        @Param("provinceId") Long provinceId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );
}
