package com.vivekai.studio.chat.service;

import com.vivekai.studio.chat.context.ConversationContextBuilder;
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
import com.vivekai.studio.provider.repository.AIProviderRepository;
import com.vivekai.studio.usage.entity.UsageLog;
import com.vivekai.studio.usage.repository.UsageLogRepository;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.user.repository.UserRepository;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatTransactionService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final AIProviderRepository providerRepository;
    private final UsageLogRepository usageLogRepository;
    private final PromptProfileVersionRepository versionRepository;
    
    private final ConversationContextBuilder contextBuilder;
    private final PromptEngine promptEngine;

    @Transactional
    public ConversationContext prepareContextAndUserMessage(UUID workspaceId, UUID conversationId, PromptRequest promptRequest, UUID activeUserId) {
        log.info("Preparing transaction context and persisting user message in workspace: {}", workspaceId);
        
        // 1. Resolve User and Workspace
        User currentUser = userRepository.findById(activeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + activeUserId));
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        if (!workspace.getOwner().getId().equals(activeUserId)) {
            throw new AccessDeniedException("User is not authorized to access this workspace");
        }

        // 2. Load or Auto-Create Conversation
        Conversation conversation;
        boolean isNew = (conversationId == null);
        if (isNew) {
            String title = promptRequest.getPrompt().substring(0, Math.min(promptRequest.getPrompt().length(), 40)) + "...";
            
            PromptProfileVersion version = null;
            if (promptRequest.getPromptProfileId() != null) {
                version = versionRepository.findFirstByProfileIdOrderByVersionNumberDesc(promptRequest.getPromptProfileId())
                        .orElseThrow(() -> new ResourceNotFoundException("No version found for prompt profile: " + promptRequest.getPromptProfileId()));
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
            log.info("Auto-created new conversation with title: {}", title);
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .filter(c -> !c.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));
            
            if (!conversation.getWorkspace().getOwner().getId().equals(activeUserId)) {
                throw new AccessDeniedException("User is not authorized to access this conversation");
            }
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
                .stream(promptRequest.isStream())
                .build();

        return ConversationContext.builder()
                .conversationId(conversation.getId())
                .chatRequest(chatRequest)
                .build();
    }

    @Transactional
    public void persistAssistantResponseAndUsage(UUID conversationId, ChatResponse chatResponse, String providerCode, UUID activeUserId) {
        log.info("Persisting assistant response and telemetry usage inside transaction");
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        UUID providerUuid = providerRepository.findByName(providerCode.toUpperCase())
                .map(AIProvider::getId)
                .orElse(null);

        Message assistantMessage = Message.builder()
                .conversation(conversation)
                .role("ASSISTANT")
                .content(chatResponse.isSuccess() ? chatResponse.getContent() : "")
                .providerId(providerUuid)
                .modelUsed(chatResponse.getModel())
                .latencyMs(chatResponse.getLatencyMs())
                .status(chatResponse.isSuccess() ? "SUCCESS" : "FAILED")
                .errorMessage(chatResponse.isSuccess() ? null : chatResponse.getErrorMessage())
                .build();

        if (chatResponse.isSuccess() && chatResponse.getUsage() != null) {
            assistantMessage.setTokenInput(chatResponse.getUsage().getPromptTokens());
            assistantMessage.setTokenOutput(chatResponse.getUsage().getCompletionTokens());
        }
        messageRepository.save(assistantMessage);

        if (chatResponse.isSuccess() && providerUuid != null) {
            TokenUsage usage = chatResponse.getUsage();
            UsageLog logEntry = UsageLog.builder()
                    .userId(activeUserId)
                    .providerId(providerUuid)
                    .modelUsed(chatResponse.getModel())
                    .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                    .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                    .latencyMs(chatResponse.getLatencyMs())
                    .cost(BigDecimal.ZERO)
                    .build();
            usageLogRepository.save(logEntry);
        }
        
        conversation.setLastMessageAt(Instant.now());
        conversationRepository.save(conversation);
    }
}
