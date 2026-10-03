package com.rehome.chatnotificationservice.serviceImpl;

import com.rehome.chatnotificationservice.dto.NotificationResponse;
import com.rehome.chatnotificationservice.entity.Notification;
import com.rehome.chatnotificationservice.enums.NotificationType;
import com.rehome.chatnotificationservice.enums.RecipientType;
import com.rehome.chatnotificationservice.event.DomainEventMessage;
import com.rehome.chatnotificationservice.mapper.NotificationMapper;
import com.rehome.chatnotificationservice.repository.NotificationRepository;
import com.rehome.chatnotificationservice.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    @Transactional
    public NotificationResponse createFromEvent(DomainEventMessage event) {

        NotificationType type = NotificationType.valueOf(event.eventType());
        RecipientType recipientType = RecipientType.valueOf(event.recipientType());

        Notification notification = Notification.builder()
                .recipientId(event.recipientId())
                .recipientType(recipientType)
                .type(type)
                .content(event.content())
                .referenceId(event.referenceId())
                .build();
        notification = notificationRepository.save(notification);

        NotificationResponse response = NotificationMapper.toResponse(notification);

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + recipientType + "/" + event.recipientId(),
                response
        );

        return response;
    }

    @Override
    public List<NotificationResponse> getNotifications(RecipientType recipientType, Long recipientId) {
        return notificationRepository.findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(recipientType, recipientId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(RecipientType recipientType, Long recipientId) {
        return notificationRepository.countByRecipientTypeAndRecipientIdAndReadAtIsNull(recipientType, recipientId);
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationMapper.toResponse(notification);

    }
}
