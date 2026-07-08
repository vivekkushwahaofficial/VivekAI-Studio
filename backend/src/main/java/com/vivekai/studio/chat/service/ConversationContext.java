package com.vivekai.studio.chat.service;

import com.vivekai.studio.provider.dto.ChatRequest;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ConversationContext {
    private UUID conversationId;
    private ChatRequest chatRequest;
}
