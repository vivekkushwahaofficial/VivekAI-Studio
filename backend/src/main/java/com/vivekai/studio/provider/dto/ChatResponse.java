package com.vivekai.studio.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String content;
    private TokenUsage usage;
    private String finishReason;
    private Long latencyMs;
    private String provider;
    private String model;
    
    @Builder.Default
    private boolean success = true;
    private String errorMessage;
}
