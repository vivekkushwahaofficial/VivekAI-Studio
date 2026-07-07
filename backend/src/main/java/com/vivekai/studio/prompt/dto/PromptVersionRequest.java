package com.vivekai.studio.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptVersionRequest {

    @NotBlank(message = "System prompt is required")
    private String systemPrompt;

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
    private String responseFormat = "text";
}
