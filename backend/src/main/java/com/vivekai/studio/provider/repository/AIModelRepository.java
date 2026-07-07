package com.vivekai.studio.provider.repository;

import com.vivekai.studio.provider.entity.AIModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIModelRepository extends JpaRepository<AIModel, UUID> {

    List<AIModel> findByProviderIdAndIsEnabledTrue(UUID providerId);

    List<AIModel> findByProviderNameAndIsEnabledTrue(String providerName);

    Optional<AIModel> findByProviderNameAndName(String providerName, String name);
}
