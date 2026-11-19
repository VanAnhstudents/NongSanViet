package com.nsv.backend.common.notification.service;

import com.nsv.backend.common.contract.InAppNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppNotificationEventListener {

    private final InAppNotificationService notificationService;

    @EventListener
    public void onInAppNotificationEvent(InAppNotificationEvent event) {
        if (event == null || event.recipientUserId() == null) {
            return;
        }
        notificationService.createNotification(
            event.recipientUserId(),
            event.type(),
            event.title(),
            event.content(),
            event.referenceType(),
            event.referenceId()
        );
    }
}
