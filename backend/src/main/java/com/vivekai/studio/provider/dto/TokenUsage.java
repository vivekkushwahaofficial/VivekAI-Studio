package com.vivekai.studio.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUsage {

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
