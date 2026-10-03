package com.rehome.chatnotificationservice.dto;

import com.rehome.chatnotificationservice.enums.MessageType;
import com.rehome.chatnotificationservice.enums.SenderType;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        SenderType senderType,
        String senderName,
        MessageType type,
        String content,
        String imageUrl,
        Long referenceId,
        Instant sentAt
) {
}
