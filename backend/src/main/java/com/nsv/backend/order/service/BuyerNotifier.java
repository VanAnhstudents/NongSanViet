package com.nsv.backend.order.service;

/** Gửi thông báo cho Buyer. Nếu module thông báo của nhóm đã có thì viết lại bản cài đặt gọi qua đó. */
public interface BuyerNotifier {

    void notifyBuyer(Long buyerId, String content);
}
