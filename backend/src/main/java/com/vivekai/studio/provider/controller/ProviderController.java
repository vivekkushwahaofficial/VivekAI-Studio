package com.vivekai.studio.provider.controller;

import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.provider.dto.ChatRequest;
import com.vivekai.studio.provider.dto.ChatResponse;
import com.vivekai.studio.provider.entity.AIProvider;
import com.vivekai.studio.provider.factory.ProviderFactory;
import com.vivekai.studio.provider.repository.AIProviderRepository;
import com.vivekai.studio.provider.strategy.AIProviderStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {

    private final AIProviderRepository providerRepository;
    private final ProviderFactory providerFactory;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AIProvider>>> listProviders() {
        log.info("Listing all available database AI providers");
        List<AIProvider> providers = providerRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(providers, "AI providers listed successfully"));
    }

    @GetMapping("/{code}/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkProviderHealth(@PathVariable String code) {
        log.info("Checking diagnostic health for provider: {}", code);
        AIProviderStrategy strategy = providerFactory.getProvider(code);
        boolean healthy = strategy.isHealthy();
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("provider", code.toUpperCase());
        healthInfo.put("healthy", healthy);
        healthInfo.put("status", healthy ? "HEALTHY" : "MOCK_MODE_ACTIVE");

        return ResponseEntity.ok(ApiResponse.success(healthInfo, "Provider health checked successfully"));
    }

    @PostMapping("/{code}/test")
    public ResponseEntity<ApiResponse<ChatResponse>> testProvider(
            @PathVariable String code,
            @Valid @RequestBody ChatRequest request
    ) {
        log.info("Executing diagnostic completion test for provider: {}", code);
        AIProviderStrategy strategy = providerFactory.getProvider(code);
        ChatResponse response = strategy.chat(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.success(response, "Provider completion test failed: " + response.getErrorMessage()));
        }

        return ResponseEntity.ok(ApiResponse.success(response, "Provider completion test executed successfully"));
    }
}
