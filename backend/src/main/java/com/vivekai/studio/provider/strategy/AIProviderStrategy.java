package com.vivekai.studio.provider.strategy;

import com.vivekai.studio.provider.dto.ChatRequest;
import com.vivekai.studio.provider.dto.ChatResponse;

public interface AIProviderStrategy {

    ChatResponse chat(ChatRequest request);

    void streamChat(ChatRequest request, java.util.function.Consumer<ChatResponse> chunkConsumer);

    String getProviderCode();

    boolean isHealthy();
}
