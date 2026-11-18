package com.nsv.backend.chat;

import com.nsv.backend.chat.dto.MessageResponse;
import com.nsv.backend.chat.dto.SendMessageRequest;
import com.nsv.backend.chat.entity.Message;
import com.nsv.backend.chat.repository.MessageRepository;
import com.nsv.backend.chat.service.ChatService;
import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.dto.UserDto;
import com.nsv.backend.user.entity.Role;
import com.nsv.backend.user.service.UserLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    private static final long BUYER_ID = 5L;
    private static final long FARMER_ID = 7L;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserLookupService userLookupService;

    private ChatService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-17T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        service = new ChatService(messageRepository, userLookupService, clock);
        when(userLookupService.findActiveById(FARMER_ID))
                .thenReturn(Optional.of(new UserDto(FARMER_ID, "Bác Ba", "0912345678", Role.FARMER)));
        when(userLookupService.findActiveById(BUYER_ID))
                .thenReturn(Optional.of(new UserDto(BUYER_ID, "Cô Lan", "0988888888", Role.BUYER)));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
    }

    private Message msg(long id, long from, long to, String content) {
        return Message.builder().id(id).senderId(from).receiverId(to).content(content)
                .sentAt(Instant.parse("2026-11-17T03:00:00Z")).build();
    }

    @Test
    @DisplayName("TC-CHAT-01: Buyer gửi tin nhắn cho Farmer -> tin nhắn được lưu kèm thời điểm gửi")
    void tc01_buyerSendsToFarmer() {
        MessageResponse result = service.send(BUYER_ID, "BUYER",
                new SendMessageRequest(FARMER_ID, "  Cà chua còn bao nhiêu kg ạ?  "));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.senderId()).isEqualTo(BUYER_ID);
        assertThat(result.receiverId()).isEqualTo(FARMER_ID);
        assertThat(result.content()).isEqualTo("Cà chua còn bao nhiêu kg ạ?");
        assertThat(result.sentAt()).isEqualTo(Instant.parse("2026-11-17T03:00:00Z"));
    }

    @Test
    @DisplayName("TC-CHAT-01: Farmer trả lời Buyer -> tin nhắn được lưu")
    void tc01_farmerRepliesToBuyer() {
        MessageResponse result = service.send(FARMER_ID, "FARMER", new SendMessageRequest(BUYER_ID, "Còn 50kg nhé"));

        assertThat(result.senderId()).isEqualTo(FARMER_ID);
        assertThat(result.receiverId()).isEqualTo(BUYER_ID);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("TC-CHAT-01: Mở lại cuộc trò chuyện -> lịch sử hiển thị đầy đủ theo thứ tự thời gian")
    void tc01_historyIsComplete() {
        when(messageRepository.findConversation(eq(BUYER_ID), eq(FARMER_ID), eq(0L), any()))
                .thenReturn(List.of(msg(1, BUYER_ID, FARMER_ID, "Cà chua còn không?"),
                        msg(2, FARMER_ID, BUYER_ID, "Còn 50kg nhé"),
                        msg(3, BUYER_ID, FARMER_ID, "Mình lấy 5kg")));

        List<MessageResponse> history = service.history(BUYER_ID, FARMER_ID, 0L, null);

        assertThat(history).extracting(MessageResponse::id).containsExactly(1L, 2L, 3L);
        assertThat(history).extracting(MessageResponse::content)
                .containsExactly("Cà chua còn không?", "Còn 50kg nhé", "Mình lấy 5kg");
    }

    @Test
    @DisplayName("TC-CHAT-01: Chỉ lấy tin mới sau afterId để cập nhật gần thực")
    void tc01_onlyNewMessagesAfterId() {
        when(messageRepository.findConversation(eq(FARMER_ID), eq(BUYER_ID), eq(2L), any()))
                .thenReturn(List.of(msg(3, BUYER_ID, FARMER_ID, "Mình lấy 5kg")));

        List<MessageResponse> news = service.history(FARMER_ID, BUYER_ID, 2L, 50);

        assertThat(news).extracting(MessageResponse::id).containsExactly(3L);
    }

    @Test
    @DisplayName("TC-CHAT-01: Không gửi được tin cho người không phải đối tác Buyer/Farmer")
    void tc01_rejectsNonBuyerFarmerPair() {
        assertThatThrownBy(() -> service.send(BUYER_ID, "BUYER", new SendMessageRequest(BUYER_ID, "alo")))
                .isInstanceOf(ApiException.class);
        when(userLookupService.findActiveById(9L))
                .thenReturn(Optional.of(new UserDto(9L, "Admin", "0900000000", Role.ADMIN)));
        assertThatThrownBy(() -> service.send(BUYER_ID, "BUYER", new SendMessageRequest(9L, "alo")))
                .isInstanceOf(ApiException.class);

        verify(messageRepository, never()).save(any(Message.class));
    }
}
