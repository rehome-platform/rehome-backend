package com.rehome.chatnotificationservice.entity;

import com.rehome.chatnotificationservice.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @Column(nullable = false)
    private Long memberId;

    private Long organizationId;

    private Long warehouseId;

    private String lastMessagePreview;

    private Instant lastMessageAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        lastMessageAt = createdAt;
    }
}