package com.rehome.chatnotificationservice.dto;

import com.rehome.chatnotificationservice.entity.Conversation;
import com.rehome.chatnotificationservice.enums.ConversationType;

import java.time.Instant;

public record ConversationSummaryResponse(
        Long id,
        ConversationType type,
        Long memberId,
        Long organizationId,
        Long warehouseId,
        String lastMessagePreview,
        Instant lastMessageAt
) {}
