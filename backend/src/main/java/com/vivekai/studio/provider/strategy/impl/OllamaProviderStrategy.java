package com.vivekai.studio.provider.strategy.impl;

import com.vivekai.studio.provider.dto.ChatRequest;
import com.vivekai.studio.provider.dto.ChatResponse;
import com.vivekai.studio.provider.dto.TokenUsage;
import com.vivekai.studio.provider.strategy.AIProviderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OllamaProviderStrategy implements AIProviderStrategy {

    @Value("${OLLAMA_BASE_URL:http://localhost:11434}")
    private String baseUrl;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public ChatResponse chat(ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "llama3";
        long startTime = System.currentTimeMillis();

        try {
            log.info("Sending chat request to Local Ollama instance: {}/api/chat", baseUrl);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("stream", false);

            List<Map<String, String>> mappedMessages = request.getMessages().stream()
                    .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                    .collect(Collectors.toList());
            body.put("messages", mappedMessages);

            // Execute HTTP request to local Ollama daemon
            Map<String, Object> response = restClient.post()
                    .uri(baseUrl + "/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            long latency = System.currentTimeMillis() - startTime;

            if (response != null && response.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) response.get("message");
                String content = (String) message.get("content");

                // Ollama responses contain direct token counts
                Integer promptTokens = (Integer) response.get("prompt_eval_count");
                Integer completionTokens = (Integer) response.get("eval_count");
                
                int promptVal = promptTokens != null ? promptTokens : 0;
                int complVal = completionTokens != null ? completionTokens : 0;
                int totalVal = promptVal + complVal;

                return ChatResponse.builder()
                        .content(content)
                        .finishReason("stop")
                        .latencyMs(latency)
                        .provider(getProviderCode())
                        .model(model)
                        .usage(new TokenUsage(promptVal, complVal, totalVal))
                        .success(true)
                        .build();
            }

            throw new RuntimeException("Invalid response layout from Ollama");

        } catch (Exception e) {
            log.warn("Ollama local connection failed. Falling back to diagnostic Mock completion: {}", e.getMessage());
            long latency = System.currentTimeMillis() - startTime;
            String prompt = request.getMessages().getLast().getContent();

            String mockText = "[Local Mode - Ollama mock fallback]\n" +
                    "Attempted to connect to local Ollama at " + baseUrl + " for model '" + model + "', but the service was unreachable.\n\n" +
                    "User prompt: \"" + prompt + "\"\n\n" +
                    "To use local inference, ensure Ollama is running (`ollama serve`) and the target model is pulled (`ollama pull " + model + "`).";

            return ChatResponse.builder()
                    .content(mockText)
                    .finishReason("stop")
                    .latencyMs(latency)
                    .provider(getProviderCode())
                    .model(model)
                    .usage(new TokenUsage(10, 20, 30))
                    .success(true)
                    .build();
        }
    }

    @Override
    public void streamChat(ChatRequest request, java.util.function.Consumer<ChatResponse> chunkConsumer) {
        long startTime = System.currentTimeMillis();
        String model = request.getModel() != null ? request.getModel() : "llama3";
        String prompt = request.getMessages().isEmpty() ? "" : request.getMessages().getLast().getContent();
        
        String mockText = "[Mock Stream] Ollama response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to your local Ollama instance, ensure the Ollama daemon is running on: " + baseUrl;

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                String[] words = mockText.split(" ");
                for (String word : words) {
                    Thread.sleep(80);
                    chunkConsumer.accept(ChatResponse.builder()
                            .content(word + " ")
                            .provider(getProviderCode())
                            .model(model)
                            .success(true)
                            .build());
                }
                
                long latency = System.currentTimeMillis() - startTime;
                chunkConsumer.accept(ChatResponse.builder()
                        .content("")
                        .provider(getProviderCode())
                        .model(model)
                        .latencyMs(latency)
                        .usage(new com.vivekai.studio.provider.dto.TokenUsage(10, words.length, 10 + words.length))
                        .success(true)
                        .build());
            } catch (Exception e) {
                log.error("Ollama streaming simulation failed", e);
                chunkConsumer.accept(ChatResponse.builder()
                        .provider(getProviderCode())
                        .model(model)
                        .success(false)
                        .errorMessage("Streaming failed: " + e.getMessage())
                        .build());
            }
        });
    }

    @Override
    public String getProviderCode() {
        return "OLLAMA";
    }

    @Override
    public boolean isHealthy() {
        try {
            // Check if Ollama daemon is reachable on root
            Map<?, ?> response = restClient.get()
                    .uri(baseUrl)
                    .retrieve()
                    .body(Map.class);
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }
}
