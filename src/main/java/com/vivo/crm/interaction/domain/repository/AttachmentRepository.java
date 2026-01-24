package com.vivo.crm.interaction.domain.repository;

import com.vivo.crm.interaction.domain.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    
    List<Attachment> findByInteractionIdOrderByCreatedAtDesc(String interactionId);
    
    void deleteByInteractionId(String interactionId);
}
