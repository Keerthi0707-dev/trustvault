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

    Optional<AuditEvent> findFirstByOrderByTimestampDescIdDesc();

    default Optional<AuditEvent> findLatestEvent() {
        return findFirstByOrderByTimestampDescIdDesc();
    }

    List<AuditEvent> findByDocumentIdOrderByTimestampDesc(UUID documentId);

    List<AuditEvent> findByActorIdOrderByTimestampDesc(UUID actorId);
}
