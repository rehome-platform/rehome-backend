package com.rehome.chatnotificationservice.dto;

import com.rehome.chatnotificationservice.enums.ConversationType;
import com.rehome.chatnotificationservice.enums.MessageType;
import com.rehome.chatnotificationservice.enums.SenderType;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(

        Long conversationId,
        ConversationType conversationType,
        @NotNull Long memberId,
        Long organizationId,
        Long warehouseId,
        @NotNull Long senderId,
        @NotNull SenderType senderType,
        @NotNull String senderName,
        @NotNull MessageType messageType,
        String content,
        String imageUrl,
        Long referenceId

        ) {}
