package com.vivo.crm.interaction.domain.repository;

import com.vivo.crm.interaction.domain.entity.PartyInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PartyInteractionRepository - JPA Repository
 */
@Repository
public interface PartyInteractionRepository extends JpaRepository<PartyInteraction, Long> {

    Optional<PartyInteraction> findByInteractionId(String interactionId);
    List<PartyInteraction> findByOriginatingPartyId(String originatingPartyId);
    List<PartyInteraction> findByStatus(String status);
    List<PartyInteraction> findByChannel(String channel);
    List<PartyInteraction> findByDirection(String direction);
    List<PartyInteraction> findByPriority(String priority);
    List<PartyInteraction> findByChannelAndStatus(String channel, String status);

    @Query("SELECT pi FROM PartyInteraction pi WHERE pi.originatingPartyId = :partyId AND pi.status IN ('initiated', 'active') ORDER BY pi.creationDate DESC")
    List<PartyInteraction> findActiveByParty(@Param("partyId") String partyId);

    @Query("SELECT pi FROM PartyInteraction pi WHERE pi.originatingPartyId = :partyId AND pi.status = 'completed' ORDER BY pi.completionDate DESC")
    List<PartyInteraction> findCompletedByParty(@Param("partyId") String partyId);

    @Query("SELECT pi FROM PartyInteraction pi WHERE pi.creationDate >= :fromDate ORDER BY pi.creationDate DESC")
    List<PartyInteraction> findCreatedAfter(@Param("fromDate") LocalDateTime fromDate);

    long countByStatus(String status);

    @Query("SELECT COUNT(pi) FROM PartyInteraction pi WHERE pi.originatingPartyId = :partyId AND pi.status IN ('initiated', 'active')")
    long countActiveByParty(@Param("partyId") String partyId);
}
