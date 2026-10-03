package com.rehome.chatnotificationservice.repository;

import com.rehome.chatnotificationservice.entity.Conversation;
import com.rehome.chatnotificationservice.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByTypeAndMemberIdAndOrganizationId(
            ConversationType type, Long memberId, Long organizationId
    );

    Optional<Conversation> findByTypeAndMemberIdAndWarehouseId(
            ConversationType type, Long memberId, Long warehouseId
    );

    List<Conversation> findByMemberIdOrderByLastMessageAtDesc(Long memberId);

    List<Conversation> findByWarehouseIdOrderByLastMessageDesc(Long warehouseId);

    List<Conversation> findByOrganizationIdOrderByLastMessageAtDesc(Long organizationId);
}
