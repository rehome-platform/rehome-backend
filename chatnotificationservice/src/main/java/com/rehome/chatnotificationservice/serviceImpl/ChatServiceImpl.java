package com.rehome.chatnotificationservice.serviceImpl;

import com.rehome.chatnotificationservice.dto.ConversationSummaryResponse;
import com.rehome.chatnotificationservice.dto.MessageResponse;
import com.rehome.chatnotificationservice.dto.SendMessageRequest;
import com.rehome.chatnotificationservice.entity.Conversation;
import com.rehome.chatnotificationservice.entity.Message;
import com.rehome.chatnotificationservice.enums.ConversationType;
import com.rehome.chatnotificationservice.mapper.ChatMapper;
import com.rehome.chatnotificationservice.repository.ConversationRepository;
import com.rehome.chatnotificationservice.repository.MessageRepository;
import com.rehome.chatnotificationservice.service.ChatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {

        Conversation conversation = resolveConversation(request);

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(request.senderId())
                .senderType(request.senderType())
                .senderName(request.senderName())
                .type(request.messageType())
                .content(request.content())
                .imageUrl(request.imageUrl())
                .referenceId(request.referenceId())
                .build();
        message = messageRepository.save(message);


        conversation.setLastMessageAt(message.getSentAt());
        conversation.setLastMessagePreview(buildPreview(message));
        conversationRepository.save(conversation);

        MessageResponse response = ChatMapper.toMessageResponse(message);

        messagingTemplate.convertAndSend("/topic/conversations/" + conversation.getId(), response);
        if (conversation.getType() == ConversationType.MEMBER_WAREHOUSE) {
            messagingTemplate.convertAndSend("/topic/warehouse/" + conversation.getWarehouseId(), response);
        }

        return response;

    }

    private Conversation resolveConversation(SendMessageRequest request) {
        if (request.conversationId() != null) {
            return conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + request.conversationId()));
        }

        if (request.conversationType() == null || request.memberId() == null) {
            throw new IllegalArgumentException("conversationType and memberId are required to start a new conversation");
        }

        return switch (request.conversationType()) {
            case MEMBER_ORGANIZATION -> {
                if (request.organizationId() == null) {
                    throw new IllegalArgumentException("organizationId is required for MEMBER_ORGANIZATION");
                }
                yield conversationRepository
                        .findByTypeAndMemberIdAndOrganizationId(ConversationType.MEMBER_ORGANIZATION, request.memberId(), request.organizationId())
                        .orElseGet(() -> conversationRepository.save(Conversation.builder()
                                .type(ConversationType.MEMBER_ORGANIZATION)
                                .memberId(request.memberId())
                                .organizationId(request.organizationId())
                                .build()));
            }
            case MEMBER_WAREHOUSE -> {
                if (request.warehouseId() == null) {
                    throw new IllegalArgumentException("warehouseId is required for MEMBER_WAREHOUSE");
                }
                yield conversationRepository
                        .findByTypeAndMemberIdAndWarehouseId(ConversationType.MEMBER_WAREHOUSE, request.memberId(), request.warehouseId())
                        .orElseGet(() -> conversationRepository.save(Conversation.builder()
                                .type(ConversationType.MEMBER_WAREHOUSE)
                                .memberId(request.memberId())
                                .warehouseId(request.warehouseId())
                                .build()));
            }
        };
    }

    private String buildPreview(Message message) {
        return switch (message.getType()) {
            case TEXT -> message.getContent();
            case IMAGE -> "[Hình ảnh]";
            case PRODUCT_REFERENCE -> "[Sản phẩm]";
            case CONSIGNMENT_REFERENCE -> "[Form ký gửi]";
        };
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(ChatMapper::toMessageResponse)
                .toList();
    }

    @Override
    public List<ConversationSummaryResponse> getConversationsOfMember(Long memberId) {
        return conversationRepository.findByMemberIdOrderByLastMessageAtDesc(memberId)
                .stream()
                .map(ChatMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public List<ConversationSummaryResponse> getConversationsOfWarehouse(Long warehouseId) {
        return conversationRepository.findByWarehouseIdOrderByLastMessageAtDesc(warehouseId)
                .stream()
                .map(ChatMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public List<ConversationSummaryResponse> getConversationsOfOrganization(Long organizationId) {
        return conversationRepository.findByOrganizationIdOrderByLastMessageAtDesc(organizationId)
                .stream()
                .map(ChatMapper::toSummaryResponse)
                .toList();
    }
}
