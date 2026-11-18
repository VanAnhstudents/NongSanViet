package com.nsv.backend.chat.service;

import com.nsv.backend.chat.dto.MessageResponse;
import com.nsv.backend.chat.dto.SendMessageRequest;
import com.nsv.backend.chat.entity.Message;
import com.nsv.backend.chat.repository.MessageRepository;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * PBI-24: chat trực tiếp giữa Buyer và Farmer.
 * Lưu mọi tin nhắn vào bảng messages; "gần thực" bằng cách client gọi lại với afterId (polling).
 * Bảng messages không có cột sản phẩm/đơn hàng nên cuộc trò chuyện được xác định theo cặp người dùng.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 200;

    private final MessageRepository messageRepository;
    private final UserLookupService userLookupService;
    private final Clock clock;

    @Transactional
    public MessageResponse send(Long senderId, String senderRole, SendMessageRequest request) {
        Long receiverId = request.receiverId();
        if (senderId.equals(receiverId)) {
            throw ApiException.badRequest("Dữ liệu không hợp lệ",
                    List.of(new FieldErrorDetail("receiverId", "Không thể nhắn tin cho chính mình")));
        }
        UserDto receiver = userLookupService.findActiveById(receiverId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy người nhận"));
        if (!isBuyerFarmerPair(senderRole, receiver.role())) {
            throw ApiException.forbidden("Chat chỉ dành cho cặp Buyer và Farmer");
        }

        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(request.content().trim())
                .sentAt(Instant.now(clock))
                .build();
        return toResponse(messageRepository.save(message));
    }

    /**
     * Lịch sử trò chuyện với một người, cũ trước mới sau. afterId = 0 để lấy từ đầu;
     * truyền id tin cuối cùng đã có để chỉ nhận tin mới (hoặc tải tiếp trang kế).
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> history(Long userId, Long partnerId, Long afterId, Integer limit) {
        int size = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        long after = afterId == null ? 0L : Math.max(afterId, 0L);
        return messageRepository.findConversation(userId, partnerId, after, PageRequest.of(0, size))
                .stream().map(this::toResponse).toList();
    }

    private static boolean isBuyerFarmerPair(String senderRole, Role receiverRole) {
        return ("BUYER".equals(senderRole) && receiverRole == Role.FARMER)
                || ("FARMER".equals(senderRole) && receiverRole == Role.BUYER);
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(m.getId(), m.getSenderId(), m.getReceiverId(), m.getContent(), m.getSentAt());
    }
}
