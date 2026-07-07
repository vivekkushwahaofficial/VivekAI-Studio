package com.vivekai.studio.provider.entity;

import com.vivekai.studio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "ai_models",
    uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIModel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private AIProvider provider;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "supports_stream", nullable = false)
    @Builder.Default
    private boolean supportsStream = true;

    @Column(name = "supports_image", nullable = false)
    @Builder.Default
    private boolean supportsImage = false;

    @Column(name = "supports_reasoning", nullable = false)
    @Builder.Default
    private boolean supportsReasoning = false;

    @Column(name = "max_tokens", nullable = false)
    @Builder.Default
    private Integer maxTokens = 4096;

    @Column(name = "default_temperature", nullable = false)
    @Builder.Default
    private Double defaultTemperature = 0.7;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean isEnabled = true;
}
