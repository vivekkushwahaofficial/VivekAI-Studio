package com.vivekai.studio.conversation.entity;

import com.vivekai.studio.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 20)
    private String role; // 'SYSTEM', 'USER', 'ASSISTANT'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "provider_id")
    private UUID providerId;

    @Column(name = "model_used", length = 100)
    private String modelUsed;

    @Column(name = "token_input")
    @Builder.Default
    private Integer tokenInput = 0;

    @Column(name = "token_output")
    @Builder.Default
    private Integer tokenOutput = 0;

    @Column(name = "latency_ms")
    @Builder.Default
    private Long latencyMs = 0L;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SUCCESS"; // 'SUCCESS', 'FAILED', 'IN_PROGRESS'

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String attachments; // Stores JSON serialized metadata list
}
