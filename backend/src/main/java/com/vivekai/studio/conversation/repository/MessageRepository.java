package com.vivekai.studio.conversation.repository;

import com.vivekai.studio.conversation.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    // Dynamic selection of history context size
    List<Message> findTop20ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
