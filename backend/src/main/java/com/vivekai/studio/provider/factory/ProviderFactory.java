package com.vivekai.studio.provider.factory;

import com.vivekai.studio.exception.ResourceNotFoundException;
import com.vivekai.studio.provider.strategy.AIProviderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProviderFactory {

    private final Map<String, AIProviderStrategy> strategies;

    public ProviderFactory(List<AIProviderStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getProviderCode().toUpperCase(),
                        strategy -> strategy
                ));
        log.info("Registered AI provider strategies: {}", strategies.keySet());
    }

    public AIProviderStrategy getProvider(String providerCode) {
        AIProviderStrategy strategy = strategies.get(providerCode.toUpperCase());
        if (strategy == null) {
            throw new ResourceNotFoundException("Unsupported AI provider: " + providerCode);
        }
        return strategy;
    }
}
