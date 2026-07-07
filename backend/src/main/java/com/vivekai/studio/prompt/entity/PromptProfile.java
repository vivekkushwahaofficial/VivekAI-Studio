package com.vivekai.studio.prompt.entity;

import com.vivekai.studio.common.entity.BaseEntity;
import com.vivekai.studio.provider.entity.AIProvider;
import com.vivekai.studio.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "prompt_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptProfile extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private AIProvider provider;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PromptCategory category;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String visibility = "PRIVATE"; // 'PRIVATE', 'WORKSPACE', 'PUBLIC'

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "prompt_profile_tags",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<PromptTag> tags = new HashSet<>();
}
