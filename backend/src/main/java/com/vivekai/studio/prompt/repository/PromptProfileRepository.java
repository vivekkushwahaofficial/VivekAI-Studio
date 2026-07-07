package com.vivekai.studio.prompt.repository;

import com.vivekai.studio.prompt.entity.PromptProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptProfileRepository extends JpaRepository<PromptProfile, UUID> {

    List<PromptProfile> findByCreatorId(UUID creatorId);

    List<PromptProfile> findByIsDefaultTrue();

    Optional<PromptProfile> findByName(String name);

    // Advanced search query filter lookup
    @Query("SELECT p FROM PromptProfile p LEFT JOIN p.tags t " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:categoryName IS NULL OR LOWER(p.category.name) = LOWER(:categoryName)) " +
           "AND (:visibility IS NULL OR p.visibility = :visibility) " +
           "AND (:tag IS NULL OR LOWER(t.name) = LOWER(:tag))")
    List<PromptProfile> searchProfiles(
            @Param("name") String name,
            @Param("categoryName") String categoryName,
            @Param("visibility") String visibility,
            @Param("tag") String tag
    );

    // Find favorites
    @Query(value = "SELECT p.* FROM prompt_profiles p " +
           "JOIN user_favorite_profiles f ON p.id = f.profile_id " +
           "WHERE f.user_id = :userId", nativeQuery = true)
    List<PromptProfile> findFavoritesForUser(@Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "INSERT INTO user_favorite_profiles (user_id, profile_id) VALUES (:userId, :profileId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addFavorite(@Param("userId") UUID userId, @Param("profileId") UUID profileId);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM user_favorite_profiles WHERE user_id = :userId AND profile_id = :profileId", nativeQuery = true)
    void removeFavorite(@Param("userId") UUID userId, @Param("profileId") UUID profileId);
}
