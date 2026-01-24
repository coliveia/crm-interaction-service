package com.vivo.crm.interaction.infrastructure.messaging;

import com.vivo.crm.interaction.application.service.InteractionService;
import com.vivo.crm.interaction.domain.model.Interaction;
import com.vivo.crm.shared.enums.InteractionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseEventConsumer {
    
    private final InteractionService interactionService;
    
    @KafkaListener(topics = "case-events", groupId = "interaction-service")
    public void consumeCaseEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("Received case event: {}", eventType);
            
            switch (eventType) {
                case "CASE_CREATED":
                    handleCaseCreated(event);
                    break;
                case "CASE_STATUS_CHANGED":
                    handleCaseStatusChanged(event);
                    break;
                case "CASE_ASSIGNED":
                    handleCaseAssigned(event);
                    break;
                default:
                    log.debug("Ignoring event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing case event: {}", e.getMessage(), e);
        }
    }
    
    private void handleCaseCreated(Map<String, Object> event) {
        String caseId = (String) event.get("caseId");
        String customerId = (String) event.get("customerId");
        
        // Create initial interaction for case creation
        Interaction interaction = Interaction.builder()
            .caseId(caseId)
            .customerId(customerId)
            .type(InteractionType.NOTE)
            .channel("SYSTEM")
            .content("Case created")
            .build();
        
        interactionService.createInteraction(interaction)
            .subscribe(
                result -> log.info("Created interaction for new case: {}", caseId),
                error -> log.error("Error creating interaction: {}", error.getMessage())
            );
    }
    
    private void handleCaseStatusChanged(Map<String, Object> event) {
        String caseId = (String) event.get("caseId");
        String oldStatus = (String) event.get("oldStatus");
        String newStatus = (String) event.get("newStatus");
        
        log.info("Case {} status changed: {} -> {}", caseId, oldStatus, newStatus);
    }
    
    private void handleCaseAssigned(Map<String, Object> event) {
        String caseId = (String) event.get("caseId");
        String agentId = (String) event.get("agentId");
        
        log.info("Case {} assigned to agent: {}", caseId, agentId);
    }
}
