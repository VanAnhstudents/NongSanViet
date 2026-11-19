package com.nsv.backend.review.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class JpaReputationRepository implements ReputationRepository {

    private static final String FARMER_ITEMS_JOIN =
            "join order_items oi on oi.id = r.order_item_id "
                    + "join batches b on b.id = oi.batch_id "
                    + "join products p on p.id = b.product_id ";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ReputationStats findStatsByFarmer(Long farmerId) {
        Object[] reviews = (Object[]) entityManager.createNativeQuery(
                        "select count(r.id), coalesce(sum(r.rating), 0), "
                                + "coalesce(sum(case when r.rating >= 4 then 1 else 0 end), 0) "
                                + "from reviews r " + FARMER_ITEMS_JOIN
                                + "where p.farmer_id = :farmerId")
                .setParameter("farmerId", farmerId)
                .getSingleResult();

        Number completed = (Number) entityManager.createNativeQuery(
                        "select count(distinct o.id) from orders o "
                                + "join order_items oi on oi.order_id = o.id "
                                + "join batches b on b.id = oi.batch_id "
                                + "join products p on p.id = b.product_id "
                                + "where p.farmer_id = :farmerId and o.status in ('DELIVERED', 'COMPLETED')")
                .setParameter("farmerId", farmerId)
                .getSingleResult();

        return new ReputationStats(((Number) reviews[0]).longValue(), ((Number) reviews[1]).longValue(),
                ((Number) reviews[2]).longValue(), completed.longValue());
    }
}
