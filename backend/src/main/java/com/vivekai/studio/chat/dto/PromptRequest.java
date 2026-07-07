package com.vivekai.studio.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptRequest {

    @NotBlank(message = "Prompt content is required")
    private String prompt;

    @NotBlank(message = "Provider code is required")
    private String providerCode; // 'OPENAI', 'GEMINI', 'CLAUDE', etc.

    @NotBlank(message = "Model name is required")
    private String modelName;

    @Builder.Default
    private boolean stream = false;

    private UUID promptProfileId;

    @Builder.Default
    private java.util.Map<String, String> variables = new java.util.HashMap<>();
}
