package com.vivo.crm.interaction.application.mapper;

import com.vivo.crm.interaction.domain.entity.PartyInteraction;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionDTO;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionCreateDTO;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionUpdateDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PartyInteractionMapper - Converte entre Entity e DTOs
 */
@Component
public class PartyInteractionMapper {

    /**
     * Converte Entity para DTO
     */
    public PartyInteractionDTO toDTO(PartyInteraction entity) {
        if (entity == null) {
            return null;
        }

        return PartyInteractionDTO.builder()
            .id(entity.getInteractionId())
            .href("/tmf-api/partyInteractionManagement/v4/partyInteraction/" + entity.getInteractionId())
            .subject(entity.getSubject())
            .description(entity.getDescription())
            .status(entity.getStatus())
            .statusChangeDate(entity.getStatusChangeDate())
            .statusChangeReason(entity.getStatusChangeReason())
            .channel(entity.getChannel())
            .direction(entity.getDirection())
            .initiationDate(entity.getInitiationDate())
            .completionDate(entity.getCompletionDate())
            .duration(entity.getDuration())
            .priority(entity.getPriority())
            .satisfaction(entity.getSatisfaction())
            .contextData(entity.getContextData())
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .type("PartyInteraction")
            .build();
    }

    /**
     * Converte DTO para Entity (criação)
     */
    public PartyInteraction toEntity(PartyInteractionCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        return PartyInteraction.builder()
            .interactionId(UUID.randomUUID().toString())
            .subject(dto.getSubject())
            .description(dto.getDescription())
            .status("initiated")
            .channel(dto.getChannel())
            .direction(dto.getDirection())
            .priority(dto.getPriority())
            .creationDate(LocalDateTime.now())
            .initiationDate(LocalDateTime.now())
            .contextData(dto.getContextData())
            .build();
    }

    /**
     * Atualiza Entity com dados do DTO (atualização parcial)
     */
    public PartyInteraction updateEntity(PartyInteraction entity, PartyInteractionUpdateDTO dto) {
        if (dto == null) {
            return entity;
        }

        if (dto.getSubject() != null) {
            entity.setSubject(dto.getSubject());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
            entity.setStatusChangeDate(LocalDateTime.now());
        }
        if (dto.getStatusChangeReason() != null) {
            entity.setStatusChangeReason(dto.getStatusChangeReason());
        }
        if (dto.getPriority() != null) {
            entity.setPriority(dto.getPriority());
        }
        if (dto.getSatisfaction() != null) {
            entity.setSatisfaction(dto.getSatisfaction());
        }
        if (dto.getCompletionDate() != null) {
            entity.setCompletionDate(dto.getCompletionDate());
        }
        if (dto.getDuration() != null) {
            entity.setDuration(dto.getDuration());
        }
        if (dto.getContextData() != null) {
            entity.setContextData(dto.getContextData());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
