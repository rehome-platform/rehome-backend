package com.rehome.chatnotificationservice.controller;

import com.rehome.chatnotificationservice.dto.ConversationSummaryResponse;
import com.rehome.chatnotificationservice.dto.MessageResponse;
import com.rehome.chatnotificationservice.dto.SendMessageRequest;
import com.rehome.chatnotificationservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(request));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }

    @GetMapping("/conversations/member/{memberId}")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversationsOfMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(chatService.getConversationsOfMember(memberId));
    }

    @GetMapping("/conversations/warehouse/{warehouseId}")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversationsOfWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(chatService.getConversationsOfWarehouse(warehouseId));
    }

    @GetMapping("/conversations/organization/{organizationId}")
    public ResponseEntity<List<ConversationSummaryResponse>> getConversationsOfOrganization(@PathVariable Long organizationId) {
        return ResponseEntity.ok(chatService.getConversationsOfOrganization(organizationId));
    }

}

