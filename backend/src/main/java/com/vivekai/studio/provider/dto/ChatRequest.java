package com.vivekai.studio.provider.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    @NotEmpty(message = "Conversation messages history cannot be empty")
    @Valid
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    private String model;

    @Builder.Default
    private Double temperature = 0.7;

    @Builder.Default
    private Integer maxTokens = 2048;

    @Builder.Default
    private Double topP = 0.9;

    @Builder.Default
    private Double presencePenalty = 0.0;

    @Builder.Default
    private Double frequencyPenalty = 0.0;

    @Builder.Default
    private boolean stream = false;

    private String systemPrompt;

    @Builder.Default
    private String responseFormat = "text"; // 'text', 'json_object'

    @Builder.Default
    private List<String> attachments = new ArrayList<>();
}
