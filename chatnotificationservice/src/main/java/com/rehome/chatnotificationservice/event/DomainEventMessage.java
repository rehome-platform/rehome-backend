package com.rehome.chatnotificationservice.event;

import java.time.Instant;

public record DomainEventMessage(
        String eventId,
        String eventType,
        String recipientType,
        Long recipientId,
        String content,
        Long referenceId,
        Instant occurredAt
) {}