package com.vivo.crm.interaction.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PartyInteraction Entity - TMF683 Party Interaction Management
 * Mapeado para Oracle Database com suporte a JSON nativo
 */
@Entity
@Table(name = "PARTY_INTERACTION", indexes = {
    @Index(name = "idx_pi_status", columnList = "status"),
    @Index(name = "idx_pi_originating_party", columnList = "originating_party_id"),
    @Index(name = "idx_pi_channel", columnList = "channel"),
    @Index(name = "idx_pi_creation_date", columnList = "creation_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interaction_id", unique = true, nullable = false, length = 50)
    private String interactionId; // UUID ou custom ID

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // initiated, active, suspended, completed, cancelled, failed

    @Column(name = "status_change_date")
    private LocalDateTime statusChangeDate;

    @Column(name = "status_change_reason", columnDefinition = "CLOB")
    private String statusChangeReason;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel; // phone, email, chat, web, mobile, socialMedia, video, inPerson, sms, whatsapp

    @Column(name = "direction", nullable = false, length = 50)
    private String direction; // inbound, outbound, internal

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "initiation_date")
    private LocalDateTime initiationDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "duration")
    private Long duration; // em segundos

    @Column(name = "priority", length = 50)
    private String priority; // high, medium, low

    @Column(name = "satisfaction", length = 50)
    private String satisfaction; // satisfied, neutral, dissatisfied

    @Column(name = "originating_party_id", length = 50)
    private String originatingPartyId; // Customer/User que iniciou

    @Column(name = "originating_party_role", length = 100)
    private String originatingPartyRole; // customer, agent, supervisor, etc.

    // JSON native support para Oracle
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_parties", columnDefinition = "JSON")
    private List<Map<String, Object>> relatedParties;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_entities", columnDefinition = "JSON")
    private List<Map<String, Object>> relatedEntities; // Tickets, Orders, etc.

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes", columnDefinition = "JSON")
    private List<Map<String, Object>> notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments", columnDefinition = "JSON")
    private List<Map<String, Object>> attachments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "characteristics", columnDefinition = "JSON")
    private List<Map<String, Object>> characteristics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "JSON")
    private Map<String, Object> contextData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "initiated";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
