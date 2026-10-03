package com.rehome.chatnotificationservice.repository;

import com.rehome.chatnotificationservice.entity.Notification;
import com.rehome.chatnotificationservice.enums.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(RecipientType recipientType, Long recipientId);

    long countByRecipientTypeAndRecipientIdAndReadAtIsNull(RecipientType recipientType, Long recipientId);
}