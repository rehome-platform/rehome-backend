package com.rehome.chatnotificationservice.dto;

import com.rehome.chatnotificationservice.enums.NotificationType;
import com.rehome.chatnotificationservice.enums.RecipientType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        RecipientType recipientType,
        Long recipientId,
        NotificationType type,
        String content,
        Long referenceId,
        Instant readAt,
        Instant createdAt
) { }
