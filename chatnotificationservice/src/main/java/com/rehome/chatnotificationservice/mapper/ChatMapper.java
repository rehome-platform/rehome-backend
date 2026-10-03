package com.rehome.chatnotificationservice.mapper;

import com.rehome.chatnotificationservice.dto.ConversationSummaryResponse;
import com.rehome.chatnotificationservice.dto.MessageResponse;
import com.rehome.chatnotificationservice.entity.Conversation;
import com.rehome.chatnotificationservice.entity.Message;

public class ChatMapper {

    private ChatMapper() {

    }

    public static MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSenderId(),
                message.getSenderType(),
                message.getSenderName(),
                message.getType(),
                message.getContent(),
                message.getImageUrl(),
                message.getReferenceId(),
                message.getSentAt()
        );
    }

    public static ConversationSummaryResponse toSummaryResponse(Conversation conversation) {
        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getMemberId(),
                conversation.getOrganizationId(),
                conversation.getWarehouseId(),
                conversation.getLastMessagePreview(),
                conversation.getLastMessageAt()
        );
    }

}
