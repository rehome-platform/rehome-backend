package com.rehome.chatnotificationservice.controller;

import com.rehome.chatnotificationservice.dto.SendMessageRequest;
import com.rehome.chatnotificationservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(SendMessageRequest request) {
        chatService.sendMessage(request);
    }

}
