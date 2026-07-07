package com.vivekai.studio.provider.controller;

import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.provider.entity.AIModel;
import com.vivekai.studio.provider.repository.AIModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
@Slf4j
public class ModelController {

    private final AIModelRepository modelRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AIModel>>> listAllModels() {
        log.info("Listing all active AI models across providers");
        List<AIModel> models = modelRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(models, "All AI models listed successfully"));
    }

    @GetMapping("/provider/{providerName}")
    public ResponseEntity<ApiResponse<List<AIModel>>> listModelsByProvider(@PathVariable String providerName) {
        log.info("Listing active models for provider: {}", providerName);
        List<AIModel> models = modelRepository.findByProviderNameAndIsEnabledTrue(providerName.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(models, "Models for " + providerName + " listed successfully"));
    }
}
