package com.vivekai.studio.provider.repository;

import com.vivekai.studio.provider.entity.AIProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIProviderRepository extends JpaRepository<AIProvider, UUID> {

    Optional<AIProvider> findByName(String name);

    Boolean existsByName(String name);
}
