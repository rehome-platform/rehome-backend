package com.rehome.chatnotificationservice.event;

import com.rehome.chatnotificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.topic.domain-events}")
    public void onDomainEvent(DomainEventMessage event) {

        try {
            notificationService.createFromEvent(event);
        } catch (IllegalArgumentException e) {
            log.warn("Bo qua even khong hop le: {} - {}", event, e.getMessage());
        }

    }

}
