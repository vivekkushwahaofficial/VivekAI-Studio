package com.vivekai.studio.chat.service;

import com.vivekai.studio.chat.dto.PromptRequest;
import com.vivekai.studio.provider.dto.ChatResponse;
import com.vivekai.studio.provider.factory.ProviderFactory;
import com.vivekai.studio.provider.strategy.AIProviderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatTransactionService chatTransactionService;
    private final ProviderFactory providerFactory;

    public ChatResponse processPrompt(UUID workspaceId, UUID conversationId, PromptRequest promptRequest, UUID activeUserId) {
        log.info("Processing prompt outside transaction for workspace: {}, conversation: {}", workspaceId, conversationId);

        // Phase 1: Prepare context and persist user message inside a short transaction
        ConversationContext context = chatTransactionService.prepareContextAndUserMessage(workspaceId, conversationId, promptRequest, activeUserId);

        // Phase 2: Call LLM provider strategy (No DB connection is occupied here!)
        AIProviderStrategy providerStrategy = providerFactory.getProvider(promptRequest.getProviderCode());
        ChatResponse chatResponse = providerStrategy.chat(context.getChatRequest());

        // Phase 3: Persist response and log usage inside a short transaction
        chatTransactionService.persistAssistantResponseAndUsage(
                context.getConversationId(),
                chatResponse,
                promptRequest.getProviderCode(),
                activeUserId
        );

        return chatResponse;
    }
}
