package com.trustvault.repository;

import com.trustvault.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    List<AuditEvent> findAllByOrderByTimestampAsc();

    List<AuditEvent> findAllByOrderByTimestampDesc();

    @Query(value = "SELECT * FROM audit_events ORDER BY timestamp DESC, id DESC LIMIT 1", nativeQuery = true)
    Optional<AuditEvent> findLatestEvent();

    List<AuditEvent> findByDocumentIdOrderByTimestampDesc(UUID documentId);

    List<AuditEvent> findByActorIdOrderByTimestampDesc(UUID actorId);
}
