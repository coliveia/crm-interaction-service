package com.vivo.crm.interaction.application.service;

import com.vivo.crm.interaction.application.mapper.PartyInteractionMapper;
import com.vivo.crm.interaction.domain.entity.PartyInteraction;
import com.vivo.crm.interaction.domain.repository.PartyInteractionRepository;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionDTO;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionCreateDTO;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PartyInteractionService - Serviço para Party Interactions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PartyInteractionService {

    private final PartyInteractionRepository interactionRepository;
    private final PartyInteractionMapper mapper;

    public PartyInteractionDTO createInteraction(PartyInteractionCreateDTO createDTO) {
        log.info("Criando nova Party Interaction: {}", createDTO.getSubject());
        PartyInteraction entity = mapper.toEntity(createDTO);
        PartyInteraction saved = interactionRepository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PartyInteractionDTO getInteractionById(String interactionId) {
        return interactionRepository.findByInteractionId(interactionId)
            .map(mapper::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PartyInteractionDTO> getInteractionsByParty(String partyId) {
        return interactionRepository.findByOriginatingPartyId(partyId)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartyInteractionDTO> getActiveInteractionsByParty(String partyId) {
        return interactionRepository.findActiveByParty(partyId)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartyInteractionDTO> getInteractionsByStatus(String status) {
        return interactionRepository.findByStatus(status)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartyInteractionDTO> getInteractionsByChannel(String channel) {
        return interactionRepository.findByChannel(channel)
            .stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }

    public PartyInteractionDTO updateInteraction(String interactionId, PartyInteractionUpdateDTO updateDTO) {
        Optional<PartyInteraction> existing = interactionRepository.findByInteractionId(interactionId);
        if (existing.isPresent()) {
            PartyInteraction entity = existing.get();
            mapper.updateEntity(entity, updateDTO);
            PartyInteraction saved = interactionRepository.save(entity);
            return mapper.toDTO(saved);
        }
        return null;
    }

    public void deleteInteraction(String interactionId) {
        Optional<PartyInteraction> existing = interactionRepository.findByInteractionId(interactionId);
        existing.ifPresent(interactionRepository::delete);
    }

    public PartyInteractionDTO changeStatus(String interactionId, String newStatus, String reason) {
        Optional<PartyInteraction> existing = interactionRepository.findByInteractionId(interactionId);
        if (existing.isPresent()) {
            PartyInteraction entity = existing.get();
            entity.setStatus(newStatus);
            entity.setStatusChangeDate(LocalDateTime.now());
            entity.setStatusChangeReason(reason);
            if ("completed".equals(newStatus)) {
                entity.setCompletionDate(LocalDateTime.now());
            }
            PartyInteraction saved = interactionRepository.save(entity);
            return mapper.toDTO(saved);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public long countActiveInteractions(String partyId) {
        return interactionRepository.countActiveByParty(partyId);
    }
}
