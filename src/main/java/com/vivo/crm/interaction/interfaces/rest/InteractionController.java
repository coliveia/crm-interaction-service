package com.vivo.crm.interaction.interfaces.rest;

import com.vivo.crm.interaction.application.dto.CreateInteractionRequest;
import com.vivo.crm.interaction.application.service.InteractionService;
import com.vivo.crm.interaction.domain.model.Interaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Tag(name = "Interactions", description = "Interaction Management APIs")
@CrossOrigin(origins = "*")
public class InteractionController {
    
    private final InteractionService interactionService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new interaction")
    public Mono<Interaction> createInteraction(@RequestBody CreateInteractionRequest request) {
        Interaction interaction = Interaction.builder()
            .caseId(request.getCaseId())
            .customerId(request.getCustomerId())
            .agentId(request.getAgentId())
            .type(request.getType())
            .channel(request.getChannel())
            .content(request.getContent())
            .audioUrl(request.getAudioUrl())
            .durationSeconds(request.getDurationSeconds())
            .metadata(request.getMetadata())
            .build();
        
        return interactionService.createInteraction(interaction);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get interaction by ID")
    public Mono<Interaction> getInteraction(@PathVariable String id) {
        return interactionService.getInteractionById(id);
    }
    
    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get all interactions for a case")
    public Flux<Interaction> getInteractionsByCase(@PathVariable String caseId) {
        return interactionService.getInteractionsByCase(caseId);
    }
    
    @GetMapping("/customer/{customerId}/timeline")
    @Operation(summary = "Get customer interaction timeline")
    public Flux<Interaction> getCustomerTimeline(@PathVariable String customerId) {
        return interactionService.getCustomerTimeline(customerId);
    }
    
    @GetMapping("/customer/{customerId}/recent")
    @Operation(summary = "Get recent customer interactions")
    public Flux<Interaction> getRecentInteractions(
        @PathVariable String customerId,
        @RequestParam(defaultValue = "30") int days
    ) {
        return interactionService.getRecentInteractions(customerId, days);
    }
    
    @PostMapping("/{id}/transcription")
    @Operation(summary = "Add transcription to interaction")
    public Mono<Interaction> addTranscription(
        @PathVariable String id,
        @RequestBody Map<String, String> request
    ) {
        return interactionService.addTranscription(id, request.get("transcription"));
    }
    
    @PostMapping("/{id}/summary")
    @Operation(summary = "Add summary to interaction")
    public Mono<Interaction> addSummary(
        @PathVariable String id,
        @RequestBody Map<String, String> request
    ) {
        return interactionService.addSummary(id, request.get("summary"));
    }
    
    @PostMapping("/{id}/sentiment")
    @Operation(summary = "Analyze sentiment of interaction")
    public Mono<Interaction> analyzeSentiment(
        @PathVariable String id,
        @RequestBody Map<String, Object> request
    ) {
        String sentiment = (String) request.get("sentiment");
        Double score = Double.parseDouble(request.get("score").toString());
        return interactionService.analyzeSentiment(id, sentiment, score);
    }
    
    @GetMapping("/case/{caseId}/stats")
    @Operation(summary = "Get interaction statistics for a case")
    public Mono<Map<String, Object>> getInteractionStats(@PathVariable String caseId) {
        return interactionService.getInteractionStats(caseId);
    }
}
