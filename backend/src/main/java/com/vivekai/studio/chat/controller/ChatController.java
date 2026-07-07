package com.vivekai.studio.chat.controller;

import com.vivekai.studio.chat.dto.PromptRequest;
import com.vivekai.studio.chat.service.ChatService;
import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.provider.dto.ChatResponse;
import com.vivekai.studio.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final com.vivekai.studio.chat.service.StreamingChatService streamingChatService;

    @PostMapping("/{workspaceId}/send")
    public ResponseEntity<ApiResponse<ChatResponse>> sendChatPrompt(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) UUID conversationId,
            @Valid @RequestBody PromptRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received prompt request from user: {} for conversation: {}", userDetails.getUsername(), conversationId);
        ChatResponse response = chatService.processPrompt(workspaceId, conversationId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Prompt completed successfully"));
    }

    @GetMapping(value = "/{workspaceId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChatResponse(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) UUID conversationId,
            @RequestParam String prompt,
            @RequestParam String providerCode,
            @RequestParam String modelName,
            @RequestParam(required = false) UUID promptProfileId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Client connected to live SSE streaming from user: {}", userDetails.getUsername());
        
        PromptRequest request = PromptRequest.builder()
                .prompt(prompt)
                .providerCode(providerCode)
                .modelName(modelName)
                .promptProfileId(promptProfileId)
                .stream(true)
                .build();

        return streamingChatService.streamPrompt(workspaceId, conversationId, request, userDetails.getId());
    }
}
