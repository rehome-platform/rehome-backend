package com.rehome.chatnotificationservice.service;

import com.rehome.chatnotificationservice.dto.NotificationResponse;
import com.rehome.chatnotificationservice.enums.RecipientType;
import com.rehome.chatnotificationservice.event.DomainEventMessage;

import java.util.List;

public interface NotificationService {


    NotificationResponse createFromEvent(DomainEventMessage event);

    List<NotificationResponse> getNotifications(RecipientType recipientType, Long recipientId);

    long getUnreadCount(RecipientType recipientType, Long recipientId);

    NotificationResponse markAsRead(Long notificationId);

}
