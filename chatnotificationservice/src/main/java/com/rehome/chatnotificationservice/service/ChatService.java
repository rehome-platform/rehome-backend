package com.rehome.chatnotificationservice.service;

import com.rehome.chatnotificationservice.dto.ConversationSummaryResponse;
import com.rehome.chatnotificationservice.dto.MessageResponse;
import com.rehome.chatnotificationservice.dto.SendMessageRequest;

import java.util.List;

public interface ChatService {

    MessageResponse sendMessage(SendMessageRequest request);

    List<MessageResponse> getMessages(Long conversationId);

    List<ConversationSummaryResponse> getConversationsOfMember(Long memberId);

    List<ConversationSummaryResponse> getConversationsOfWarehouse(Long warehouseId);

    List<ConversationSummaryResponse> getConversationsOfOrganization(Long organizationId);

}
