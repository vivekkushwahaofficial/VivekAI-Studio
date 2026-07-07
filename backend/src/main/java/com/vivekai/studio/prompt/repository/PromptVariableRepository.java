package com.vivekai.studio.prompt.repository;

import com.vivekai.studio.prompt.entity.PromptVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromptVariableRepository extends JpaRepository<PromptVariable, UUID> {

    List<PromptVariable> findByProfileId(UUID profileId);
}
