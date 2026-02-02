package com.vivo.crm.interaction.interfaces.controller;

import com.vivo.crm.interaction.application.service.PartyInteractionService;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionDTO;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionCreateDTO;
import com.vivo.crm.shared.dto.tmf683.PartyInteractionUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PartyInteractionController - REST API TMF683
 */
@Slf4j
@RestController
@RequestMapping("/tmf-api/partyInteractionManagement/v4")
@RequiredArgsConstructor
public class PartyInteractionController {

    private final PartyInteractionService interactionService;

    @PostMapping("/partyInteraction")
    public ResponseEntity<PartyInteractionDTO> createPartyInteraction(@RequestBody PartyInteractionCreateDTO createDTO) {
        log.info("POST /partyInteraction - Criando nova Party Interaction");
        PartyInteractionDTO result = interactionService.createInteraction(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/partyInteraction/{id}")
    public ResponseEntity<PartyInteractionDTO> getPartyInteraction(@PathVariable String id) {
        log.info("GET /partyInteraction/{} - Recuperando Party Interaction", id);
        PartyInteractionDTO result = interactionService.getInteractionById(id);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/partyInteraction")
    public ResponseEntity<List<PartyInteractionDTO>> listPartyInteractions(
        @RequestParam(required = false) String partyId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String channel,
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("GET /partyInteraction - Listando Party Interactions");

        List<PartyInteractionDTO> results;
        if (partyId != null && activeOnly) {
            results = interactionService.getActiveInteractionsByParty(partyId);
        } else if (partyId != null) {
            results = interactionService.getInteractionsByParty(partyId);
        } else if (status != null) {
            results = interactionService.getInteractionsByStatus(status);
        } else if (channel != null) {
            results = interactionService.getInteractionsByChannel(channel);
        } else {
            results = List.of();
        }
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/partyInteraction/{id}")
    public ResponseEntity<PartyInteractionDTO> updatePartyInteraction(
        @PathVariable String id,
        @RequestBody PartyInteractionUpdateDTO updateDTO) {
        log.info("PATCH /partyInteraction/{} - Atualizando Party Interaction", id);
        PartyInteractionDTO result = interactionService.updateInteraction(id, updateDTO);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/partyInteraction/{id}")
    public ResponseEntity<Void> deletePartyInteraction(@PathVariable String id) {
        log.info("DELETE /partyInteraction/{} - Deletando Party Interaction", id);
        interactionService.deleteInteraction(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/partyInteraction/{id}/status")
    public ResponseEntity<PartyInteractionDTO> changeInteractionStatus(
        @PathVariable String id,
        @RequestParam String status,
        @RequestParam(required = false) String reason) {
        log.info("PATCH /partyInteraction/{}/status - Mudando status para: {}", id, status);
        PartyInteractionDTO result = interactionService.changeStatus(id, status, reason);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/partyInteraction/party/{partyId}/active")
    public ResponseEntity<List<PartyInteractionDTO>> getActiveInteractionsByParty(@PathVariable String partyId) {
        log.info("GET /partyInteraction/party/{}/active - Listando interações ativas", partyId);
        List<PartyInteractionDTO> results = interactionService.getActiveInteractionsByParty(partyId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/partyInteraction/party/{partyId}/count")
    public ResponseEntity<Long> countActiveInteractions(@PathVariable String partyId) {
        log.info("GET /partyInteraction/party/{}/count - Contando interações ativas", partyId);
        long count = interactionService.countActiveInteractions(partyId);
        return ResponseEntity.ok(count);
    }
}
