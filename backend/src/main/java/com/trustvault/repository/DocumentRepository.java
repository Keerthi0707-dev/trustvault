package com.trustvault.repository;

import com.trustvault.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndIsDeletedFalse(UUID id);

    List<Document> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID ownerId);

    @Query("""
        SELECT DISTINCT d FROM Document d
        LEFT JOIN DocumentGrant g ON g.document = d AND g.revocationTimestamp IS NULL
        WHERE d.isDeleted = false
        AND (d.owner.id = :userId OR g.recipientUser.id = :userId)
        ORDER BY d.createdAt DESC
    """)
    List<Document> findAllAccessibleToUser(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT d FROM Document d
        LEFT JOIN DocumentGrant g ON g.document = d AND g.revocationTimestamp IS NULL
        WHERE d.isDeleted = false
        AND (d.owner.id = :userId OR g.recipientUser.id = :userId)
        AND (:category IS NULL OR LOWER(d.category) = LOWER(:category))
        AND (:searchQuery IS NULL OR LOWER(d.originalDisplayName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) 
             OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))
             OR LOWER(d.owner.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
        ORDER BY d.createdAt DESC
    """)
    List<Document> searchAccessibleToUser(@Param("userId") UUID userId,
                                         @Param("searchQuery") String searchQuery,
                                         @Param("category") String category);

    boolean existsBySafeStorageObjectKey(String safeStorageObjectKey);
}
