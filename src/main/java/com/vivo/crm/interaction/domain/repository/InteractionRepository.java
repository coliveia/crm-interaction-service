package com.vivo.crm.interaction.domain.repository;

import com.vivo.crm.interaction.domain.model.Interaction;
import com.vivo.crm.shared.enums.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, String> {
    
    List<Interaction> findByCaseIdOrderByCreatedAtDesc(String caseId);
    
    List<Interaction> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    List<Interaction> findByAgentIdAndCreatedAtBetween(
        String agentId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    List<Interaction> findByTypeAndCreatedAtAfter(
        InteractionType type, 
        LocalDateTime after
    );
    
    @Query("SELECT i FROM Interaction i WHERE i.customerId = :customerId " +
           "AND i.createdAt >= :since ORDER BY i.createdAt DESC")
    List<Interaction> findRecentByCustomer(
        @Param("customerId") String customerId,
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT COUNT(i) FROM Interaction i WHERE i.caseId = :caseId")
    Long countByCaseId(@Param("caseId") String caseId);
    
    @Query("SELECT i FROM Interaction i WHERE i.sentiment = :sentiment " +
           "AND i.createdAt >= :since ORDER BY i.sentimentScore DESC")
    List<Interaction> findBySentimentSince(
        @Param("sentiment") String sentiment,
        @Param("since") LocalDateTime since
    );
}
