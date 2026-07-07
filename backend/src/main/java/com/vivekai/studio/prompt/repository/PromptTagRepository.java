package com.vivekai.studio.prompt.repository;

import com.vivekai.studio.prompt.entity.PromptTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTagRepository extends JpaRepository<PromptTag, UUID> {

    Optional<PromptTag> findByName(String name);
}
