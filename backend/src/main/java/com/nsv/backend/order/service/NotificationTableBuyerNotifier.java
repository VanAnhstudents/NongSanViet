package com.nsv.backend.order.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/** Ghi thông báo vào bảng notifications (user_id, content, is_read = false). */
@Component
public class NotificationTableBuyerNotifier implements BuyerNotifier {

    private static final int MAX_CONTENT = 500;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void notifyBuyer(Long buyerId, String content) {
        String text = content.length() > MAX_CONTENT ? content.substring(0, MAX_CONTENT) : content;
        entityManager.createNativeQuery("insert into notifications (user_id, content, is_read, created_at) "
                        + "values (:userId, :content, false, now())")
                .setParameter("userId", buyerId)
                .setParameter("content", text)
                .executeUpdate();
    }
}
