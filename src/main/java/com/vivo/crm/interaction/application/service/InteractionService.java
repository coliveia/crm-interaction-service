package com.vivo.crm.interaction.application.service;

import com.vivo.crm.interaction.domain.model.Interaction;
import com.vivo.crm.interaction.domain.repository.InteractionRepository;
import com.vivo.crm.shared.enums.InteractionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {
    
    private final InteractionRepository interactionRepository;
    private final AIService aiService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    public Mono<Interaction> createInteraction(Interaction interaction) {
        return Mono.fromCallable(() -> {
            log.info("Creating interaction for case: {}", interaction.getCaseId());
            
            Interaction saved = interactionRepository.save(interaction);
            
            // Publish event
            publishInteractionEvent("INTERACTION_CREATED", saved);
            
            // Process asynchronously
            if (saved.hasAudio()) {
                processAudioAsync(saved.getId());
            } else if (saved.getContent() != null) {
                processTextAsync(saved.getId());
            }
            
            return saved;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<Interaction> getInteractionById(String id) {
        return Mono.fromCallable(() -> 
            interactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interaction not found: " + id))
        ).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Flux<Interaction> getInteractionsByCase(String caseId) {
        return Mono.fromCallable(() -> 
            interactionRepository.findByCaseIdOrderByCreatedAtDesc(caseId)
        )
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }
    
    public Flux<Interaction> getCustomerTimeline(String customerId) {
        return Mono.fromCallable(() -> 
            interactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
        )
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }
    
    public Flux<Interaction> getRecentInteractions(String customerId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return Mono.fromCallable(() -> 
            interactionRepository.findRecentByCustomer(customerId, since)
        )
        .flatMapMany(Flux::fromIterable)
        .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Transactional
    public Mono<Interaction> addTranscription(String interactionId, String transcription) {
        return Mono.fromCallable(() -> {
            Interaction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new RuntimeException("Interaction not found"));
            
            interaction.addTranscription(transcription);
            Interaction updated = interactionRepository.save(interaction);
            
            // Generate summary from transcription
            processSummaryAsync(interactionId);
            
            return updated;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Transactional
    public Mono<Interaction> addSummary(String interactionId, String summary) {
        return Mono.fromCallable(() -> {
            Interaction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new RuntimeException("Interaction not found"));
            
            interaction.addSummary(summary);
            return interactionRepository.save(interaction);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Transactional
    public Mono<Interaction> analyzeSentiment(String interactionId, String sentiment, Double score) {
        return Mono.fromCallable(() -> {
            Interaction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new RuntimeException("Interaction not found"));
            
            interaction.analyzeSentiment(sentiment, score);
            Interaction updated = interactionRepository.save(interaction);
            
            publishInteractionEvent("SENTIMENT_ANALYZED", updated);
            
            return updated;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    public Mono<Map<String, Object>> getInteractionStats(String caseId) {
        return Mono.fromCallable(() -> {
            List<Interaction> interactions = interactionRepository.findByCaseIdOrderByCreatedAtDesc(caseId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", interactions.size());
            stats.put("withTranscription", interactions.stream().filter(Interaction::hasTranscription).count());
            stats.put("avgDuration", interactions.stream()
                .filter(i -> i.getDurationSeconds() != null)
                .mapToInt(Interaction::getDurationSeconds)
                .average()
                .orElse(0));
            
            Map<String, Long> byType = new HashMap<>();
            for (InteractionType type : InteractionType.values()) {
                long count = interactions.stream()
                    .filter(i -> i.getType() == type)
                    .count();
                byType.put(type.name(), count);
            }
            stats.put("byType", byType);
            
            Map<String, Long> bySentiment = new HashMap<>();
            bySentiment.put("POSITIVE", interactions.stream().filter(i -> "POSITIVE".equals(i.getSentiment())).count());
            bySentiment.put("NEUTRAL", interactions.stream().filter(i -> "NEUTRAL".equals(i.getSentiment())).count());
            bySentiment.put("NEGATIVE", interactions.stream().filter(i -> "NEGATIVE".equals(i.getSentiment())).count());
            stats.put("bySentiment", bySentiment);
            
            return stats;
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    // Async processing methods
    private void processAudioAsync(String interactionId) {
        Mono.fromRunnable(() -> {
            try {
                log.info("Processing audio for interaction: {}", interactionId);
                // Transcription would be done here
                // For now, just log
            } catch (Exception e) {
                log.error("Error processing audio: {}", e.getMessage());
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
    }
    
    private void processTextAsync(String interactionId) {
        aiService.generateSummary(interactionId)
            .flatMap(summary -> addSummary(interactionId, summary))
            .flatMap(interaction -> aiService.analyzeSentiment(interaction.getContent())
                .flatMap(result -> analyzeSentiment(
                    interactionId, 
                    result.get("sentiment").toString(),
                    Double.parseDouble(result.get("score").toString())
                ))
            )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                result -> log.info("Text processing completed for: {}", interactionId),
                error -> log.error("Error processing text: {}", error.getMessage())
            );
    }
    
    private void processSummaryAsync(String interactionId) {
        aiService.generateSummary(interactionId)
            .flatMap(summary -> addSummary(interactionId, summary))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                result -> log.info("Summary generated for: {}", interactionId),
                error -> log.error("Error generating summary: {}", error.getMessage())
            );
    }
    
    private void publishInteractionEvent(String eventType, Interaction interaction) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("interactionId", interaction.getId());
            event.put("caseId", interaction.getCaseId());
            event.put("customerId", interaction.getCustomerId());
            event.put("type", interaction.getType());
            event.put("timestamp", LocalDateTime.now());
            
            kafkaTemplate.send("interaction-events", interaction.getId(), event);
            log.info("Published event: {} for interaction: {}", eventType, interaction.getId());
        } catch (Exception e) {
            log.error("Error publishing event: {}", e.getMessage());
        }
    }
}
