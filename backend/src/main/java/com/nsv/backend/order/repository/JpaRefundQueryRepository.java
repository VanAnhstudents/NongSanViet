package com.nsv.backend.order.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class JpaRefundQueryRepository implements RefundQueryRepository {

    private static final String FARMER_ITEMS =
            "select 1 from order_items oi join batches b on b.id = oi.batch_id "
                    + "join products p on p.id = b.product_id where oi.order_id = :orderId ";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean orderBelongsToFarmer(Long orderId, Long farmerId) {
        return exists("select exists (" + FARMER_ITEMS + "and p.farmer_id = :farmerId)", orderId, farmerId);
    }

    @Override
    public boolean orderHasOtherFarmers(Long orderId, Long farmerId) {
        return exists("select exists (" + FARMER_ITEMS + "and p.farmer_id <> :farmerId)", orderId, farmerId);
    }

    @Override
    public boolean isPreOrder(Long orderId) {
        return exists("select exists (select 1 from payments pay where pay.order_id = :orderId "
                        + "and pay.payment_type = 'DEPOSIT') "
                        + "or exists (select 1 from order_items oi join batches b on b.id = oi.batch_id "
                        + "where oi.order_id = :orderId and b.status = 'PRE_ORDER')",
                orderId, null);
    }

    private boolean exists(String sql, Long orderId, Long farmerId) {
        var query = entityManager.createNativeQuery(sql).setParameter("orderId", orderId);
        if (farmerId != null) {
            query.setParameter("farmerId", farmerId);
        }
        return Boolean.TRUE.equals(query.getSingleResult());
    }
}
