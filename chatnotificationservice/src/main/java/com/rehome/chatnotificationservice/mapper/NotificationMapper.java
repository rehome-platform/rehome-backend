package com.rehome.chatnotificationservice.mapper;

import com.rehome.chatnotificationservice.dto.NotificationResponse;
import com.rehome.chatnotificationservice.entity.Notification;

public class NotificationMapper {

    private NotificationMapper() {}

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientType(),
                notification.getRecipientId(),
                notification.getType(),
                notification.getContent(),
                notification.getReferenceId(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

}
