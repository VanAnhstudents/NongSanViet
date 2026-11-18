package com.nsv.backend.chat.controller;

import com.nsv.backend.chat.dto.MessageResponse;
import com.nsv.backend.chat.dto.SendMessageRequest;
import com.nsv.backend.chat.service.ChatService;
import com.nsv.backend.common.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** PBI-24: chat mua - bán, dùng chung cho Buyer và Farmer (cần đăng nhập). */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SendMessageRequest request) {
        return chatService.send(user.id(), user.role(), request);
    }

    @GetMapping("/conversations/{partnerId}/messages")
    public List<MessageResponse> history(@AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") Long afterId,
            @RequestParam(required = false) Integer limit) {
        return chatService.history(user.id(), partnerId, afterId, limit);
    }
}
