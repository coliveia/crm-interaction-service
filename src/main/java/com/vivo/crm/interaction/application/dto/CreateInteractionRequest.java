package com.vivo.crm.interaction.application.dto;

import com.vivo.crm.shared.enums.InteractionType;
import lombok.Data;

@Data
public class CreateInteractionRequest {
    private String caseId;
    private String customerId;
    private String agentId;
    private InteractionType type;
    private String channel;
    private String content;
    private String audioUrl;
    private Integer durationSeconds;
    private String metadata;
}
