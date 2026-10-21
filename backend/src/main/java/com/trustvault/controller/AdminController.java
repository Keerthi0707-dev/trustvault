package com.trustvault.controller;

import com.trustvault.dto.AuditEventDto;
import com.trustvault.dto.AuditIntegrityReportDto;
import com.trustvault.dto.UserDto;
import com.trustvault.dto.UserStatusUpdateRequest;
import com.trustvault.entity.AuditEvent;
import com.trustvault.entity.User;
import com.trustvault.exception.AccessDeniedException;
import com.trustvault.repository.AuditEventRepository;
import com.trustvault.repository.UserRepository;
import com.trustvault.security.SecurityUtils;
import com.trustvault.service.AuditService;
import com.trustvault.service.DocumentAuthorizationService;
import com.trustvault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final DocumentAuthorizationService authorizationService;

    public AdminController(UserService userService,
                           AuditService auditService,
                           AuditEventRepository auditEventRepository,
                           UserRepository userRepository,
                           DocumentAuthorizationService authorizationService) {
        this.userService = userService;
        this.auditService = auditService;
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        User currentUser = getCurrentUser();
        List<UserDto> users = userService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable("id") UUID id,
                                                   @Valid @RequestBody UserStatusUpdateRequest request) {
        User currentUser = getCurrentUser();
        UserDto updated = userService.updateUserStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditEventDto>> getGlobalAuditEvents() {
        User currentUser = getCurrentUser();
        authorizationService.checkAdmin(currentUser);

        List<AuditEvent> events = auditEventRepository.findAllByOrderByTimestampDesc();

        Map<UUID, String> emailMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getEmail, (a, b) -> a));

        List<AuditEventDto> dtos = events.stream().map(e -> new AuditEventDto(
                e.getId(),
                e.getTimestamp(),
                e.getActorId(),
                e.getActorId() != null ? emailMap.get(e.getActorId()) : "System/Anonymous",
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

    @GetMapping("/audit/integrity")
    public ResponseEntity<AuditIntegrityReportDto> checkAuditIntegrity() {
        User currentUser = getCurrentUser();
        authorizationService.checkAdmin(currentUser);

        AuditIntegrityReportDto report = auditService.verifyAuditIntegrity();
        return ResponseEntity.ok(report);
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
    }
}
