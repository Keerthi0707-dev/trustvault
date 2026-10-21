package com.trustvault.controller;

import com.trustvault.dto.AuditEventDto;
import com.trustvault.entity.AuditEvent;
import com.trustvault.entity.Document;
import com.trustvault.entity.User;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.repository.AuditEventRepository;
import com.trustvault.repository.DocumentRepository;
import com.trustvault.repository.UserRepository;
import com.trustvault.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final AuditEventRepository auditEventRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public ActivityController(AuditEventRepository auditEventRepository,
                              DocumentRepository documentRepository,
                              UserRepository userRepository) {
        this.auditEventRepository = auditEventRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditEventDto>> getUserActivity() {
        User currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        List<Document> myDocs = documentRepository.findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(currentUser.getId());
        Set<UUID> myDocIds = myDocs.stream().map(Document::getId).collect(Collectors.toSet());

        List<AuditEvent> allEvents = auditEventRepository.findAllByOrderByTimestampDesc();

        Map<UUID, String> emailMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getEmail, (a, b) -> a));

        List<AuditEventDto> dtos = allEvents.stream()
                .filter(e -> currentUser.getId().equals(e.getActorId())
                        || currentUser.getId().equals(e.getTargetUserId())
                        || (e.getDocumentId() != null && myDocIds.contains(e.getDocumentId())))
                .limit(100)
                .map(e -> new AuditEventDto(
                        e.getId(),
                        e.getTimestamp(),
                        e.getActorId(),
                        e.getActorId() != null ? emailMap.get(e.getActorId()) : "System",
                        e.getAction(),
                        e.getDocumentId(),
                        e.getTargetUserId(),
                        e.getTargetUserId() != null ? emailMap.get(e.getTargetUserId()) : null,
                        e.getOutcome(),
                        e.getCorrelationId(),
                        e.getDetails(),
                        e.getPreviousEventHash(),
                        e.getCurrentEventHash()
                )).toList();

        return ResponseEntity.ok(dtos);
    }
}
