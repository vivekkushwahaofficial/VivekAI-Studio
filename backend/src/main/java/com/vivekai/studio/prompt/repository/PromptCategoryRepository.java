package com.vivekai.studio.prompt.repository;

import com.vivekai.studio.prompt.entity.PromptCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptCategoryRepository extends JpaRepository<PromptCategory, Integer> {

    Optional<PromptCategory> findByName(String name);
}
