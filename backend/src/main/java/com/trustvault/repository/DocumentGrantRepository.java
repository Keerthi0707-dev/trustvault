package com.trustvault.repository;

import com.trustvault.entity.DocumentGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentGrantRepository extends JpaRepository<DocumentGrant, UUID> {

    List<DocumentGrant> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);

    @Query("""
        SELECT g FROM DocumentGrant g 
        WHERE g.document.id = :documentId 
        AND g.recipientUser.id = :recipientUserId 
        AND g.revocationTimestamp IS NULL
    """)
    Optional<DocumentGrant> findActiveGrant(@Param("documentId") UUID documentId, 
                                           @Param("recipientUserId") UUID recipientUserId);

    @Query("""
        SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END 
        FROM DocumentGrant g 
        WHERE g.document.id = :documentId 
        AND g.recipientUser.id = :recipientUserId 
        AND g.revocationTimestamp IS NULL
    """)
    boolean hasActiveGrant(@Param("documentId") UUID documentId, 
                           @Param("recipientUserId") UUID recipientUserId);

    List<DocumentGrant> findByRecipientUserIdAndRevocationTimestampIsNull(UUID recipientUserId);
}
