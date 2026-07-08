package com.vivekai.studio.conversation.service;

import com.vivekai.studio.conversation.entity.Conversation;
import com.vivekai.studio.conversation.entity.Message;
import com.vivekai.studio.conversation.repository.ConversationRepository;
import com.vivekai.studio.conversation.repository.MessageRepository;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.repository.WorkspaceRepository;
import com.vivekai.studio.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WorkspaceRepository workspaceRepository;

    public List<Conversation> getConversationsForWorkspace(UUID workspaceId, UUID userId) {
        log.info("Fetching conversations for workspace: {} for user: {}", workspaceId, userId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        if (!workspace.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to access conversations in this workspace");
        }

        return conversationRepository.findByWorkspaceIdAndIsDeletedFalseOrderByLastMessageAtDesc(workspaceId);
    }

    public List<Message> getMessagesForConversation(UUID conversationId, UUID userId) {
        log.info("Fetching messages for conversation: {} for user: {}", conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        if (!conversation.getWorkspace().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to access messages in this conversation");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Conversation pinConversation(UUID conversationId, boolean pin, UUID userId) {
        log.info("Toggling pin status of conversation: {} to: {} for user: {}", conversationId, pin, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        if (!conversation.getWorkspace().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to pin this conversation");
        }

        conversation.setPinned(pin);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public Conversation favoriteConversation(UUID conversationId, boolean favorite, UUID userId) {
        log.info("Toggling favorite status of conversation: {} to: {} for user: {}", conversationId, favorite, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        if (!conversation.getWorkspace().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to favorite this conversation");
        }

        conversation.setFavorite(favorite);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public void deleteConversation(UUID conversationId, UUID userId) {
        log.info("Soft deleting conversation: {} for user: {}", conversationId, userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        if (!conversation.getWorkspace().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to delete this conversation");
        }

        conversation.setDeleted(true);
        conversationRepository.save(conversation);
    }
}
