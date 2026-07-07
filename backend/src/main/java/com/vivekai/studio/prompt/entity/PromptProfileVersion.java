package com.vivekai.studio.prompt.entity;

import com.vivekai.studio.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "prompt_profile_versions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "version_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptProfileVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private PromptProfile profile;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(nullable = false)
    @Builder.Default
    private Double temperature = 0.7;

    @Column(name = "max_tokens", nullable = false)
    @Builder.Default
    private Integer maxTokens = 2048;

    @Column(name = "top_p", nullable = false)
    @Builder.Default
    private Double topP = 0.9;

    @Column(name = "presence_penalty", nullable = false)
    @Builder.Default
    private Double presencePenalty = 0.0;

    @Column(name = "frequency_penalty", nullable = false)
    @Builder.Default
    private Double frequencyPenalty = 0.0;

    @Column(name = "response_format", nullable = false, length = 20)
    @Builder.Default
    private String responseFormat = "text"; // 'text', 'json_object'

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;
}
