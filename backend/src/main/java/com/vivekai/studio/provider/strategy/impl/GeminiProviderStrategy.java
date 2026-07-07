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
public class GeminiProviderStrategy implements AIProviderStrategy {

    @Value("${GEMINI_API_KEY:your_gemini_api_key_here}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public ChatResponse chat(ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "gemini-1.5-flash";
        long startTime = System.currentTimeMillis();

        if (isMockMode()) {
            return getMockResponse(model, request, startTime);
        }

        try {
            log.info("Sending chat request to Gemini with model: {}", model);

            // Structure contents matching Google Gemini API spec
            List<Map<String, Object>> contents = request.getMessages().stream()
                    .map(msg -> {
                        Map<String, Object> contentMap = new HashMap<>();
                        // Gemini expects 'user' or 'model' roles. Translate 'assistant' to 'model'
                        String role = msg.getRole().equalsIgnoreCase("assistant") ? "model" : "user";
                        contentMap.put("role", role);
                        contentMap.put("parts", List.of(Map.of("text", msg.getContent())));
                        return contentMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", request.getTemperature());
            generationConfig.put("maxOutputTokens", request.getMaxTokens());
            generationConfig.put("topP", request.getTopP());

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);
            body.put("generationConfig", generationConfig);

            if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
                Map<String, Object> systemInstruction = new HashMap<>();
                systemInstruction.put("parts", List.of(Map.of("text", request.getSystemPrompt())));
                body.put("systemInstruction", systemInstruction);
            }

            // Execute HTTP request
            Map<String, Object> response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            long latency = System.currentTimeMillis() - startTime;

            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                Map<String, Object> candidate = candidates.getFirst();
                Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                String content = (String) parts.getFirst().get("text");
                String finishReason = (String) candidate.get("finishReason");

                Map<String, Object> usageMetadata = (Map<String, Object>) response.get("usageMetadata");
                Integer promptTokens = (Integer) usageMetadata.get("promptTokenCount");
                Integer completionTokens = (Integer) usageMetadata.get("candidatesTokenCount");
                Integer totalTokens = (Integer) usageMetadata.get("totalTokenCount");

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

            throw new RuntimeException("Invalid response layout from Gemini");

        } catch (Exception e) {
            log.error("Gemini request failed: {}", e.getMessage());
            return ChatResponse.builder()
                    .provider(getProviderCode())
                    .model(model)
                    .success(false)
                    .errorMessage("Gemini API request failed: " + e.getMessage())
                    .latencyMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public void streamChat(ChatRequest request, java.util.function.Consumer<ChatResponse> chunkConsumer) {
        long startTime = System.currentTimeMillis();
        String model = request.getModel() != null ? request.getModel() : "gemini-1.5-flash";
        String prompt = request.getMessages().isEmpty() ? "" : request.getMessages().getLast().getContent();
        
        String mockText = "[Mock Stream] Gemini response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to the live Gemini API, define a valid 'GEMINI_API_KEY' in your environment configuration.";

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
                        .usage(new com.vivekai.studio.provider.dto.TokenUsage(12, words.length, 12 + words.length))
                        .success(true)
                        .build());
            } catch (Exception e) {
                log.error("Gemini streaming simulation failed", e);
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
        return "GEMINI";
    }

    @Override
    public boolean isHealthy() {
        return !isMockMode();
    }

    private boolean isMockMode() {
        return apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your_gemini_api_key_here");
    }

    private ChatResponse getMockResponse(String model, ChatRequest request, long startTime) {
        log.info("Executing Gemini strategy in Mock Mode (API Key missing)");
        long latency = System.currentTimeMillis() - startTime;
        String prompt = request.getMessages().getLast().getContent();

        String mockText = "[Mock Mode] Gemini response for prompt: \"" + prompt + "\"\n\n" +
                "To connect to the live Gemini API, define a valid 'GEMINI_API_KEY' in your environment configuration.";

        return ChatResponse.builder()
                .content(mockText)
                .finishReason("STOP")
                .latencyMs(latency)
                .provider(getProviderCode())
                .model(model)
                .usage(new TokenUsage(15, 30, 45))
                .success(true)
                .build();
    }
}
