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
public class OpenAIProviderStrategy implements AIProviderStrategy {

    @Value("${OPENAI_API_KEY:your_openai_api_key_here}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public ChatResponse chat(ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "gpt-4o-mini";
        long startTime = System.currentTimeMillis();

        if (isMockMode()) {
            return getMockResponse(model, request, startTime);
        }

        try {
            log.info("Sending chat request to OpenAI with model: {}", model);
            
            // Map request payload
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", request.getTemperature());
            body.put("max_tokens", request.getMaxTokens());
            body.put("top_p", request.getTopP());
            body.put("presence_penalty", request.getPresencePenalty());
            body.put("frequency_penalty", request.getFrequencyPenalty());
            
            List<Map<String, String>> mappedMessages = request.getMessages().stream()
                    .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                    .collect(Collectors.toList());
            body.put("messages", mappedMessages);

            // Execute HTTP request
            Map<String, Object> response = restClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            long latency = System.currentTimeMillis() - startTime;

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> choice = choices.getFirst();
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                String content = (String) message.get("content");
                String finishReason = (String) choice.get("finish_reason");

                Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                Integer promptTokens = (Integer) usage.get("prompt_tokens");
                Integer completionTokens = (Integer) usage.get("completion_tokens");
                Integer totalTokens = (Integer) usage.get("total_tokens");

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

            throw new RuntimeException("Invalid response from OpenAI");

        } catch (Exception e) {
            log.error("OpenAI request failed: {}", e.getMessage());
            return ChatResponse.builder()
                    .provider(getProviderCode())
                    .model(model)
                    .success(false)
                    .errorMessage("OpenAI API request failed: " + e.getMessage())
                    .latencyMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public void streamChat(ChatRequest request, java.util.function.Consumer<ChatResponse> chunkConsumer) {
        long startTime = System.currentTimeMillis();
        String model = request.getModel() != null ? request.getModel() : "gpt-4o";
        String prompt = request.getMessages().isEmpty() ? "" : request.getMessages().getLast().getContent();
        
        String mockText = "[Mock Stream] OpenAI response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to the live OpenAI API, define a valid 'OPENAI_API_KEY' in your environment configuration.";

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
                        .usage(new com.vivekai.studio.provider.dto.TokenUsage(15, words.length, 15 + words.length))
                        .success(true)
                        .build());
            } catch (Exception e) {
                log.error("OpenAI streaming simulation failed", e);
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
        return "OPENAI";
    }

    @Override
    public boolean isHealthy() {
        return !isMockMode();
    }

    private boolean isMockMode() {
        return apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your_openai_api_key_here");
    }

    private ChatResponse getMockResponse(String model, ChatRequest request, long startTime) {
        log.info("Executing OpenAI strategy in Mock Mode (API Key missing)");
        long latency = System.currentTimeMillis() - startTime;
        String prompt = request.getMessages().getLast().getContent();
        
        String mockText = "[Mock Mode] OpenAI response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to the live OpenAI API, define a valid 'OPENAI_API_KEY' in your environment configuration.";

        return ChatResponse.builder()
                .content(mockText)
                .finishReason("stop")
                .latencyMs(latency)
                .provider(getProviderCode())
                .model(model)
                .usage(new TokenUsage(12, 45, 57))
                .success(true)
                .build();
    }
}
