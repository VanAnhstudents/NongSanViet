package com.nsv.backend.product.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class JpaProductModerationRepository implements ProductModerationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean productExists(Long productId) {
        Number count = (Number) entityManager
                .createNativeQuery("select count(*) from products where id = :id")
                .setParameter("id", productId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public int closeAllBatches(Long productId) {
        return entityManager
                .createNativeQuery("update batches set status = 'CLOSED' where product_id = :id and status <> 'CLOSED'")
                .setParameter("id", productId)
                .executeUpdate();
    }
}
