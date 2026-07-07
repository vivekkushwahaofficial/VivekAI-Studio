package com.vivekai.studio.prompt.repository;

import com.vivekai.studio.prompt.entity.PromptProfileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptProfileVersionRepository extends JpaRepository<PromptProfileVersion, UUID> {

    List<PromptProfileVersion> findByProfileIdOrderByVersionNumberDesc(UUID profileId);

    Optional<PromptProfileVersion> findByProfileIdAndVersionNumber(UUID profileId, Integer versionNumber);

    Optional<PromptProfileVersion> findFirstByProfileIdOrderByVersionNumberDesc(UUID profileId);
}
