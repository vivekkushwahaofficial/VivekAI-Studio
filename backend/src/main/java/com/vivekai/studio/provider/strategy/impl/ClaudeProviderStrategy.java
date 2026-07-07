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
public class ClaudeProviderStrategy implements AIProviderStrategy {

    @Value("${CLAUDE_API_KEY:your_claude_api_key_here}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public ChatResponse chat(ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "claude-3-5-sonnet-latest";
        long startTime = System.currentTimeMillis();

        if (isMockMode()) {
            return getMockResponse(model, request, startTime);
        }

        try {
            log.info("Sending chat request to Claude with model: {}", model);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("max_tokens", request.getMaxTokens());
            body.put("temperature", request.getTemperature());

            if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
                body.put("system", request.getSystemPrompt());
            }

            List<Map<String, String>> mappedMessages = request.getMessages().stream()
                    // Filter system messages out of messages body since Claude expects system text as a root param
                    .filter(msg -> !msg.getRole().equalsIgnoreCase("system"))
                    .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                    .collect(Collectors.toList());
            body.put("messages", mappedMessages);

            // Execute HTTP request
            Map<String, Object> response = restClient.post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            long latency = System.currentTimeMillis() - startTime;

            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> contentList = (List<Map<String, Object>>) response.get("content");
                Map<String, Object> contentMap = contentList.getFirst();
                String content = (String) contentMap.get("text");
                String finishReason = (String) response.get("stop_reason");

                Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                Integer promptTokens = (Integer) usage.get("input_tokens");
                Integer completionTokens = (Integer) usage.get("output_tokens");
                Integer totalTokens = promptTokens + completionTokens;

                return ChatResponse.builder()
                        .content(content)
                        .finishReason(finishReason)
                        .latencyMs(latency)
                        .provider(getProviderCode())
                        .model(model)
                        .usage(new TokenUsage(promptTokens, completionTokens, totalTokens))
                        .success(true)
                        .build();
            }

            throw new RuntimeException("Invalid response layout from Anthropic");

        } catch (Exception e) {
            log.error("Claude request failed: {}", e.getMessage());
            return ChatResponse.builder()
                    .provider(getProviderCode())
                    .model(model)
                    .success(false)
                    .errorMessage("Claude API request failed: " + e.getMessage())
                    .latencyMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public void streamChat(ChatRequest request, java.util.function.Consumer<ChatResponse> chunkConsumer) {
        long startTime = System.currentTimeMillis();
        String model = request.getModel() != null ? request.getModel() : "claude-3-5-sonnet";
        String prompt = request.getMessages().isEmpty() ? "" : request.getMessages().getLast().getContent();
        
        String mockText = "[Mock Stream] Claude response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to the live Claude API, define a valid 'CLAUDE_API_KEY' in your environment configuration.";

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
                log.error("Claude streaming simulation failed", e);
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
        return "CLAUDE";
    }

    @Override
    public boolean isHealthy() {
        return !isMockMode();
    }

    private boolean isMockMode() {
        return apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your_claude_api_key_here");
    }

    private ChatResponse getMockResponse(String model, ChatRequest request, long startTime) {
        log.info("Executing Claude strategy in Mock Mode (API Key missing)");
        long latency = System.currentTimeMillis() - startTime;
        String prompt = request.getMessages().getLast().getContent();

        String mockText = "[Mock Mode] Claude response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to the live Claude API, define a valid 'CLAUDE_API_KEY' in your environment configuration.";

        return ChatResponse.builder()
                .content(mockText)
                .finishReason("end_turn")
                .latencyMs(latency)
                .provider(getProviderCode())
                .model(model)
                .usage(new TokenUsage(10, 20, 30))
                .success(true)
                .build();
    }
}
