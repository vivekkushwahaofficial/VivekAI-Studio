package com.vivekai.studio.chat.context;

import com.vivekai.studio.conversation.entity.Conversation;
import com.vivekai.studio.conversation.entity.Message;
import com.vivekai.studio.conversation.repository.MessageRepository;
import com.vivekai.studio.provider.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationContextBuilder {

    private final MessageRepository messageRepository;

    public List<ChatMessage> buildContext(Conversation conversation, String newPrompt, String systemInstruction) {
        log.info("Building model request context window for conversation: {}", conversation.getId());
        List<ChatMessage> context = new ArrayList<>();

        // 1. Prepend System Prompt / Custom Profile system instructions first
        if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
            context.add(ChatMessage.builder()
                    .role("system")
                    .content(systemInstruction)
                    .build());
        }

        // 2. Fetch the last 20 historical messages (returns latest first, so we reverse it)
        List<Message> historyDesc = messageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(conversation.getId());
        List<Message> historyAsc = new ArrayList<>(historyDesc);
        Collections.reverse(historyAsc);

        // 3. Map database messages to strategy DTO message layout
        List<ChatMessage> historicalContext = historyAsc.stream()
                .map(msg -> ChatMessage.builder()
                        .role(msg.getRole().toLowerCase())
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList());

        context.addAll(historicalContext);

        // 4. Append current user prompt as final content
        context.add(ChatMessage.builder()
                .role("user")
                .content(newPrompt)
                .build());

        log.debug("Built request context containing {} elements", context.size());
        return context;
    }
}
