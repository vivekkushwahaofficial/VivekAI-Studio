package com.vivekai.studio.conversation.repository;

import com.vivekai.studio.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByWorkspaceIdAndIsDeletedFalseOrderByLastMessageAtDesc(UUID workspaceId);

    List<Conversation> findByCreatorIdAndIsDeletedFalse(UUID userId);
}
