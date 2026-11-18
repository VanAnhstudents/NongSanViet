package com.nsv.backend.review.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaReviewQueryRepository implements ReviewQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<String> findOrderStatusForBuyer(Long orderItemId, Long buyerId) {
        List<?> rows = entityManager.createNativeQuery(
                        "select o.status from order_items oi join orders o on o.id = oi.order_id "
                                + "where oi.id = :orderItemId and o.buyer_id = :buyerId")
                .setParameter("orderItemId", orderItemId)
                .setParameter("buyerId", buyerId)
                .getResultList();
        return rows.isEmpty() ? Optional.empty() : Optional.of(String.valueOf(rows.get(0)));
    }

    @Override
    public boolean productExists(Long productId) {
        Number count = (Number) entityManager
                .createNativeQuery("select count(*) from products where id = :id")
                .setParameter("id", productId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public List<ProductReviewRow> findByProduct(Long productId) {
        List<?> rows = entityManager.createNativeQuery(
                        "select r.id, r.rating, r.comment, r.created_at, u.full_name "
                                + "from reviews r "
                                + "join order_items oi on oi.id = r.order_item_id "
                                + "join batches b on b.id = oi.batch_id "
                                + "join orders o on o.id = oi.order_id "
                                + "join users u on u.id = o.buyer_id "
                                + "where b.product_id = :productId order by r.id desc")
                .setParameter("productId", productId)
                .getResultList();
        return rows.stream().map(row -> {
            Object[] a = (Object[]) row;
            return new ProductReviewRow(((Number) a[0]).longValue(), ((Number) a[1]).intValue(),
                    (String) a[2], toInstant(a[3]), (String) a[4]);
        }).toList();
    }

    private static Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.toInstant(ZoneOffset.UTC);
        }
        return null;
    }
}
