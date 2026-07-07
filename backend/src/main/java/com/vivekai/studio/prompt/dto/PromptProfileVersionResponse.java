package com.vivekai.studio.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptProfileVersionResponse {

    private UUID id;
    private Integer versionNumber;
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private Double presencePenalty;
    private Double frequencyPenalty;
    private String responseFormat;
    private Instant createdAt;
    private String createdByUsername;
}
