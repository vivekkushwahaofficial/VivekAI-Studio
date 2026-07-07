package com.vivekai.studio.conversation.controller;

import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.conversation.entity.Conversation;
import com.vivekai.studio.conversation.entity.Message;
import com.vivekai.studio.conversation.repository.ConversationRepository;
import com.vivekai.studio.conversation.repository.MessageRepository;
import com.vivekai.studio.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations(@PathVariable UUID workspaceId) {
        log.info("Fetching conversations for workspace: {}", workspaceId);
        List<Conversation> conversations = conversationRepository
                .findByWorkspaceIdAndIsDeletedFalseOrderByLastMessageAtDesc(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(conversations, "Conversations retrieved successfully"));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(@PathVariable UUID conversationId) {
        log.info("Fetching messages for conversation: {}", conversationId);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages, "Messages retrieved successfully"));
    }

    @PatchMapping("/{conversationId}/pin")
    @Transactional
    public ResponseEntity<ApiResponse<Conversation>> pinConversation(
            @PathVariable UUID conversationId,
            @RequestParam boolean pin
    ) {
        log.info("Toggling pin status of conversation: {} to: {}", conversationId, pin);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));
        conversation.setPinned(pin);
        Conversation updated = conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiResponse.success(updated, "Conversation pin toggled successfully"));
    }

    @PatchMapping("/{conversationId}/favorite")
    @Transactional
    public ResponseEntity<ApiResponse<Conversation>> favoriteConversation(
            @PathVariable UUID conversationId,
            @RequestParam boolean favorite
    ) {
        log.info("Toggling favorite status of conversation: {} to: {}", conversationId, favorite);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));
        conversation.setFavorite(favorite);
        Conversation updated = conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiResponse.success(updated, "Conversation favorite toggled successfully"));
    }

    @DeleteMapping("/{conversationId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> softDeleteConversation(@PathVariable UUID conversationId) {
        log.info("Soft deleting conversation: {}", conversationId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));
        conversation.setDeleted(true);
        conversationRepository.save(conversation);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully"));
    }
}
