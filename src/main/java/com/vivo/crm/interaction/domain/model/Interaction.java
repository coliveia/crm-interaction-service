package com.vivo.crm.interaction.domain.model;

import com.vivo.crm.shared.enums.InteractionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "case_id", nullable = false)
    private String caseId;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Column(name = "agent_id")
    private String agentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;
    
    @Column(nullable = false)
    private String channel; // PHONE, EMAIL, CHAT, WEB, MOBILE
    
    @Column(columnDefinition = "CLOB")
    private String content;
    
    @Column(columnDefinition = "CLOB")
    private String transcription;
    
    @Column(columnDefinition = "CLOB")
    private String summary;
    
    @Column(name = "sentiment")
    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
    
    @Column(name = "sentiment_score")
    private Double sentimentScore;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "audio_url")
    private String audioUrl;
    
    @Column(name = "attachment_count")
    private Integer attachmentCount = 0;
    
    @Column(columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public void addTranscription(String transcription) {
        this.transcription = transcription;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addSummary(String summary) {
        this.summary = summary;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void analyzeSentiment(String sentiment, Double score) {
        this.sentiment = sentiment;
        this.sentimentScore = score;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean hasAudio() {
        return audioUrl != null && !audioUrl.isEmpty();
    }
    
    public boolean hasTranscription() {
        return transcription != null && !transcription.isEmpty();
    }
}
