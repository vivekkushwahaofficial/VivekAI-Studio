package com.vivekai.studio.prompt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptProfileRequest {

    @NotBlank(message = "Profile name is required")
    private String name;

    private String description;
    private String icon;

    @NotBlank(message = "Provider code is required")
    private String providerCode;

    @NotBlank(message = "Model name is required")
    private String modelName;

    private String categoryName;

    @Builder.Default
    private String visibility = "PRIVATE"; // 'PRIVATE', 'WORKSPACE', 'PUBLIC'

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    // Initial Version properties
    @NotBlank(message = "Initial system prompt is required")
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

    // Variables declarations schema
    @Builder.Default
    @Valid
    private List<PromptVariableRequest> variables = new ArrayList<>();
}
