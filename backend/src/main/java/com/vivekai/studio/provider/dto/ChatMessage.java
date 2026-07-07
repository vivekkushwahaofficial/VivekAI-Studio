package com.vivekai.studio.provider.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @NotBlank(message = "Role is required (e.g. 'system', 'user', 'assistant')")
    private String role;

    @NotBlank(message = "Message content is required")
    private String content;
}
