package com.vivekai.studio.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptProfileResponse {

    private UUID id;
    private String name;
    private String description;
    private String icon;
    private String providerName;
    private String modelName;
    private boolean isDefault;
    private String categoryName;
    private String visibility;
    private String creatorUsername;
    
    private Set<String> tags;
    private List<PromptVariableResponse> variables;
    private PromptProfileVersionResponse latestVersion;
}
