package com.vivekai.studio.chat.dto;

import com.vivekai.studio.provider.dto.TokenUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatStreamEvent {

    private ChatEventType type;
    private String content;
    private Integer promptTokens;
    private Integer completionTokens;
    private Long latencyMs;

    public static ChatStreamEvent start() {
        return ChatStreamEvent.builder()
                .type(ChatEventType.START)
                .build();
    }

    public static ChatStreamEvent token(String chunk) {
        return ChatStreamEvent.builder()
                .type(ChatEventType.TOKEN)
                .content(chunk)
                .build();
    }

    public static ChatStreamEvent finish(TokenUsage usage, Long latencyMs) {
        return ChatStreamEvent.builder()
                .type(ChatEventType.FINISH)
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .latencyMs(latencyMs)
                .build();
    }

    public static ChatStreamEvent error(String message) {
        return ChatStreamEvent.builder()
                .type(ChatEventType.ERROR)
                .content(message)
                .build();
    }
}
