package com.vivo.crm.interaction.application.service;

import com.vivo.crm.interaction.domain.model.Interaction;
import com.vivo.crm.interaction.domain.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {
    
    private final InteractionRepository interactionRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String openaiApiUrl;
    
    public Mono<String> generateSummary(String interactionId) {
        return Mono.fromCallable(() -> 
            interactionRepository.findById(interactionId)
                .orElseThrow(() -> new RuntimeException("Interaction not found"))
        )
        .flatMap(interaction -> {
            String content = interaction.hasTranscription() 
                ? interaction.getTranscription() 
                : interaction.getContent();
            
            if (content == null || content.isEmpty()) {
                return Mono.just("No content to summarize");
            }
            
            return callOpenAI(
                "Summarize the following customer interaction in 2-3 sentences:\n\n" + content,
                "gpt-4.1-mini",
                150
            );
        });
    }
    
    public Mono<Map<String, Object>> analyzeSentiment(String content) {
        if (content == null || content.isEmpty()) {
            return Mono.just(Map.of("sentiment", "NEUTRAL", "score", 0.5));
        }
        
        return callOpenAI(
            "Analyze the sentiment of this customer interaction. " +
            "Respond with only: POSITIVE, NEUTRAL, or NEGATIVE\n\n" + content,
            "gpt-4.1-mini",
            10
        )
        .map(sentiment -> {
            String normalized = sentiment.trim().toUpperCase();
            double score = switch (normalized) {
                case "POSITIVE" -> 0.9;
                case "NEGATIVE" -> 0.1;
                default -> 0.5;
            };
            
            Map<String, Object> result = new HashMap<>();
            result.put("sentiment", normalized);
            result.put("score", score);
            return result;
        });
    }
    
    public Mono<List<String>> extractKeyPoints(String content) {
        if (content == null || content.isEmpty()) {
            return Mono.just(List.of());
        }
        
        return callOpenAI(
            "Extract 3-5 key points from this customer interaction. " +
            "Return as a bullet list:\n\n" + content,
            "gpt-4.1-mini",
            200
        )
        .map(response -> {
            return List.of(response.split("\n"));
        });
    }
    
    public Mono<String> suggestNextAction(String interactionId) {
        return Mono.fromCallable(() -> 
            interactionRepository.findById(interactionId)
                .orElseThrow(() -> new RuntimeException("Interaction not found"))
        )
        .flatMap(interaction -> {
            String context = buildContextForSuggestion(interaction);
            
            return callOpenAI(
                "Based on this customer interaction, suggest the next best action " +
                "for the agent:\n\n" + context,
                "gpt-4.1-mini",
                100
            );
        });
    }
    
    private String buildContextForSuggestion(Interaction interaction) {
        StringBuilder context = new StringBuilder();
        context.append("Type: ").append(interaction.getType()).append("\n");
        context.append("Channel: ").append(interaction.getChannel()).append("\n");
        
        if (interaction.getSentiment() != null) {
            context.append("Sentiment: ").append(interaction.getSentiment()).append("\n");
        }
        
        if (interaction.hasTranscription()) {
            context.append("Content: ").append(interaction.getTranscription());
        } else if (interaction.getContent() != null) {
            context.append("Content: ").append(interaction.getContent());
        }
        
        return context.toString();
    }
    
    private Mono<String> callOpenAI(String prompt, String model, int maxTokens) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            log.warn("OpenAI API key not configured, returning mock response");
            return Mono.just("Mock AI response (configure OPENAI_API_KEY)");
        }
        
        WebClient webClient = webClientBuilder
            .baseUrl(openaiApiUrl)
            .defaultHeader("Authorization", "Bearer " + openaiApiKey)
            .build();
        
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));
        request.put("max_tokens", maxTokens);
        request.put("temperature", 0.7);
        
        return webClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
                return "No response from AI";
            })
            .onErrorResume(error -> {
                log.error("Error calling OpenAI: {}", error.getMessage());
                return Mono.just("Error generating AI response");
            });
    }
}
