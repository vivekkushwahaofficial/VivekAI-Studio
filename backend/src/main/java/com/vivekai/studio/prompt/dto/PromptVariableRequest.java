package com.vivekai.studio.prompt.dto;

import com.vivekai.studio.prompt.entity.VariableType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptVariableRequest {

    @NotBlank(message = "Variable name is required")
    private String name;

    private String description;

    @Builder.Default
    private boolean required = true;

    private String defaultValue;

    @NotNull(message = "Variable type is required")
    private VariableType type;
}
