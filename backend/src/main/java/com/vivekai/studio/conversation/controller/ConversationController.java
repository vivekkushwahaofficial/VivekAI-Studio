package com.vivekai.studio.conversation.controller;

import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.conversation.entity.Conversation;
import com.vivekai.studio.conversation.entity.Message;
import com.vivekai.studio.conversation.service.ConversationService;
import com.vivekai.studio.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    private final ConversationService conversationService;

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Fetching conversations for workspace: {} from user: {}", workspaceId, userDetails.getUsername());
        List<Conversation> conversations = conversationService.getConversationsForWorkspace(workspaceId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(conversations, "Conversations retrieved successfully"));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Fetching messages for conversation: {} from user: {}", conversationId, userDetails.getUsername());
        List<Message> messages = conversationService.getMessagesForConversation(conversationId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(messages, "Messages retrieved successfully"));
    }

    @PatchMapping("/{conversationId}/pin")
    public ResponseEntity<ApiResponse<Conversation>> pinConversation(
            @PathVariable UUID conversationId,
            @RequestParam boolean pin,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to pin conversation: {} to: {} from user: {}", conversationId, pin, userDetails.getUsername());
        Conversation updated = conversationService.pinConversation(conversationId, pin, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(updated, "Conversation pin toggled successfully"));
    }

    @PatchMapping("/{conversationId}/favorite")
    public ResponseEntity<ApiResponse<Conversation>> favoriteConversation(
            @PathVariable UUID conversationId,
            @RequestParam boolean favorite,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to favorite conversation: {} to: {} from user: {}", conversationId, favorite, userDetails.getUsername());
        Conversation updated = conversationService.favoriteConversation(conversationId, favorite, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(updated, "Conversation favorite toggled successfully"));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> softDeleteConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to delete conversation: {} from user: {}", conversationId, userDetails.getUsername());
        conversationService.deleteConversation(conversationId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully"));
    }
}
