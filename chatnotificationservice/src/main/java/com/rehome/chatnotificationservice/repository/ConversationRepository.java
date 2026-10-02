package com.rehome.chatnotificationservice.repository;

import com.rehome.chatnotificationservice.entity.Conversation;
import com.rehome.chatnotificationservice.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByTypeAndMemberIdAndOrganizationId(
            ConversationType type, Long memberId, Long organizationId
    );




}
