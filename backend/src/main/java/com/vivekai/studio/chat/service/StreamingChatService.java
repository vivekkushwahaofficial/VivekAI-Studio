package com.vivekai.studio.chat.service;

import com.vivekai.studio.chat.context.ConversationContextBuilder;
import com.vivekai.studio.chat.dto.ChatEventType;
import com.vivekai.studio.chat.dto.ChatStreamEvent;
import com.vivekai.studio.chat.dto.PromptRequest;
import com.vivekai.studio.conversation.entity.Conversation;
import com.vivekai.studio.conversation.entity.Message;
import com.vivekai.studio.conversation.repository.ConversationRepository;
import com.vivekai.studio.conversation.repository.MessageRepository;
import com.vivekai.studio.exception.ResourceNotFoundException;
import com.vivekai.studio.prompt.engine.PromptEngine;
import com.vivekai.studio.prompt.entity.PromptProfileVersion;
import com.vivekai.studio.prompt.repository.PromptProfileVersionRepository;
import com.vivekai.studio.provider.dto.ChatRequest;
import com.vivekai.studio.provider.dto.ChatResponse;
import com.vivekai.studio.provider.dto.TokenUsage;
import com.vivekai.studio.provider.entity.AIProvider;
import com.vivekai.studio.provider.factory.ProviderFactory;
import com.vivekai.studio.provider.repository.AIProviderRepository;
import com.vivekai.studio.provider.strategy.AIProviderStrategy;
import com.vivekai.studio.usage.entity.UsageLog;
import com.vivekai.studio.usage.repository.UsageLogRepository;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.user.repository.UserRepository;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final AIProviderRepository providerRepository;
    private final UsageLogRepository usageLogRepository;
    private final PromptProfileVersionRepository versionRepository;
    
    private final ConversationContextBuilder contextBuilder;
    private final ProviderFactory providerFactory;
    private final PromptEngine promptEngine;

    @Transactional
    public SseEmitter streamPrompt(UUID workspaceId, UUID conversationId, PromptRequest promptRequest, UUID activeUserId) {
        log.info("Initializing SSE stream request from user: {} for conversation: {}", activeUserId, conversationId);
        
        SseEmitter emitter = new SseEmitter(180000L); // 3-minute timeout
        
        // 1. Resolve User and Workspace
        User currentUser = userRepository.findById(activeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + activeUserId));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        // 2. Load or Auto-Create Conversation
        Conversation conversation;
        boolean isNew = (conversationId == null);
        if (isNew) {
            String title = promptRequest.getPrompt().substring(0, Math.min(promptRequest.getPrompt().length(), 40)) + "...";
            PromptProfileVersion version = null;
            if (promptRequest.getPromptProfileId() != null) {
                version = versionRepository.findFirstByProfileIdOrderByVersionNumberDesc(promptRequest.getPromptProfileId())
                        .orElse(null);
            }

            conversation = Conversation.builder()
                    .title(title)
                    .workspace(workspace)
                    .creator(currentUser)
                    .promptProfileVersion(version)
                    .isPinned(false)
                    .isFavorite(false)
                    .isArchived(false)
                    .isDeleted(false)
                    .lastMessageAt(Instant.now())
                    .build();
            conversation = conversationRepository.save(conversation);
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .filter(c -> !c.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));
        }

        // 3. Persist User Prompt Message
        Message userMessage = Message.builder()
                .conversation(conversation)
                .role("USER")
                .content(promptRequest.getPrompt())
                .status("SUCCESS")
                .build();
        messageRepository.save(userMessage);

        // 4. Resolve Dynamic Snapshotted parameters & Variables
        String systemInstruction = "You are a helpful AI assistant.";
        Double temp = 0.7;
        Integer maxTok = 2048;
        Double topPVal = 0.9;

        if (conversation.getPromptProfileVersion() != null) {
            PromptProfileVersion ver = conversation.getPromptProfileVersion();
            temp = ver.getTemperature();
            maxTok = ver.getMaxTokens();
            topPVal = ver.getTopP();
            if (ver.getSystemPrompt() != null) {
                systemInstruction = promptEngine.resolveTemplate(ver.getSystemPrompt(), promptRequest.getVariables());
            }
        }

        List<com.vivekai.studio.provider.dto.ChatMessage> chatHistory = contextBuilder.buildContext(
                conversation,
                promptRequest.getPrompt(),
                systemInstruction
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(chatHistory)
                .model(promptRequest.getModelName())
                .temperature(temp)
                .maxTokens(maxTok)
                .topP(topPVal)
                .stream(true)
                .build();

        // Send START event
        try {
            emitter.send(SseEmitter.event().name("START").data(ChatStreamEvent.start()));
        } catch (IOException e) {
            log.error("Failed to send SSE START event", e);
        }

        // 5. Invoke Asynchronous Stream
        AIProviderStrategy providerStrategy = providerFactory.getProvider(promptRequest.getProviderCode());
        UUID providerUuid = providerRepository.findByName(promptRequest.getProviderCode().toUpperCase())
                .map(AIProvider::getId)
                .orElse(null);

        // Track accumulated content
        StringBuilder accumulatedContent = new StringBuilder();
        Conversation finalConversation = conversation;

        providerStrategy.streamChat(chatRequest, chunkResponse -> {
            try {
                if (chunkResponse.isSuccess()) {
                    if (chunkResponse.getContent() != null && !chunkResponse.getContent().isEmpty()) {
                        accumulatedContent.append(chunkResponse.getContent());
                        emitter.send(SseEmitter.event()
                                .name("TOKEN")
                                .data(ChatStreamEvent.token(chunkResponse.getContent())));
                    }
                    
                    // Final payload containing statistics
                    if (chunkResponse.getUsage() != null || chunkResponse.getLatencyMs() != null) {
                        TokenUsage usage = chunkResponse.getUsage();
                        Long latency = chunkResponse.getLatencyMs();
                        
                        emitter.send(SseEmitter.event()
                                .name("FINISH")
                                .data(ChatStreamEvent.finish(usage, latency)));

                        // Save assistant message to Database (separate self-contained repositories)
                        saveAssistantMessage(finalConversation, accumulatedContent.toString(), providerUuid, chunkResponse.getModel(), usage, latency);
                        
                        // Log usage metrics
                        logUsageMetrics(currentUser.getId(), providerUuid, chunkResponse.getModel(), usage, latency);
                        
                        emitter.complete();
                    }
                } else {
                    emitter.send(SseEmitter.event()
                            .name("ERROR")
                            .data(ChatStreamEvent.error(chunkResponse.getErrorMessage())));
                    emitter.complete();
                }
            } catch (IOException ex) {
                log.warn("SSE connection closed by client during response stream");
                emitter.completeWithError(ex);
            }
        });

        // Update conversation last message timestamp
        conversation.setLastMessageAt(Instant.now());
        conversationRepository.save(conversation);

        return emitter;
    }

    private void saveAssistantMessage(Conversation conversation, String content, UUID providerId, String model, TokenUsage usage, Long latencyMs) {
        Message assistantMessage = Message.builder()
                .conversation(conversation)
                .role("ASSISTANT")
                .content(content)
                .providerId(providerId)
                .modelUsed(model)
                .latencyMs(latencyMs)
                .status("SUCCESS")
                .tokenInput(usage != null ? usage.getPromptTokens() : 0)
                .tokenOutput(usage != null ? usage.getCompletionTokens() : 0)
                .build();
        messageRepository.save(assistantMessage);
    }

    private void logUsageMetrics(UUID userId, UUID providerId, String model, TokenUsage usage, Long latencyMs) {
        if (providerId == null) return;
        UsageLog logEntry = UsageLog.builder()
                .userId(userId)
                .providerId(providerId)
                .modelUsed(model)
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .latencyMs(latencyMs)
                .cost(BigDecimal.ZERO)
                .build();
        usageLogRepository.save(logEntry);
    }
}
