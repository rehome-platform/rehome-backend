package com.rehome.chatnotificationservice.controller;

import com.rehome.chatnotificationservice.dto.NotificationResponse;
import com.rehome.chatnotificationservice.enums.RecipientType;
import com.rehome.chatnotificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{recipientType}/{recipientId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId) {
        return ResponseEntity.ok(notificationService.getNotifications(recipientType, recipientId));
    }

    @GetMapping("/{recipientType}/{recipientId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable RecipientType recipientType,
            @PathVariable Long recipientId) {
        long count = notificationService.getUnreadCount(recipientType, recipientId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

}
