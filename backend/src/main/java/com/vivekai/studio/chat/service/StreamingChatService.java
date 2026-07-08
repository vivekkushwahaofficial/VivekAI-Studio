package com.vivekai.studio.chat.service;

import com.vivekai.studio.chat.dto.ChatStreamEvent;
import com.vivekai.studio.chat.dto.PromptRequest;
import com.vivekai.studio.provider.dto.ChatResponse;
import com.vivekai.studio.provider.dto.TokenUsage;
import com.vivekai.studio.provider.factory.ProviderFactory;
import com.vivekai.studio.provider.strategy.AIProviderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingChatService {

    private final ChatTransactionService chatTransactionService;
    private final ProviderFactory providerFactory;

    public SseEmitter streamPrompt(UUID workspaceId, UUID conversationId, PromptRequest promptRequest, UUID activeUserId) {
        log.info("Initializing SSE stream request outside transaction from user: {} for workspace: {}", activeUserId, workspaceId);
        
        SseEmitter emitter = new SseEmitter(180000L); // 3-minute timeout
        
        // Phase 1: Prepare database context and user message (short transactional phase)
        ConversationContext context = chatTransactionService.prepareContextAndUserMessage(workspaceId, conversationId, promptRequest, activeUserId);

        // Send START event
        try {
            emitter.send(SseEmitter.event().name("START").data(ChatStreamEvent.start()));
        } catch (IOException e) {
            log.error("Failed to send SSE START event", e);
        }

        // Phase 2: Call streaming LLM provider strategy (completely transaction-free!)
        AIProviderStrategy providerStrategy = providerFactory.getProvider(promptRequest.getProviderCode());
        StringBuilder accumulatedContent = new StringBuilder();

        providerStrategy.streamChat(context.getChatRequest(), chunkResponse -> {
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

                        // Phase 3: Persist assistant response and log usage inside a short transaction
                        ChatResponse response = ChatResponse.builder()
                                .success(true)
                                .content(accumulatedContent.toString())
                                .model(chunkResponse.getModel())
                                .latencyMs(latency)
                                .usage(usage)
                                .build();

                        chatTransactionService.persistAssistantResponseAndUsage(
                                context.getConversationId(),
                                response,
                                promptRequest.getProviderCode(),
                                activeUserId
                        );
                        
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

        return emitter;
    }
}
