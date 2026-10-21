package com.trustvault.service;

import com.trustvault.dto.UserDto;
import com.trustvault.entity.AuditAction;
import com.trustvault.entity.AuditOutcome;
import com.trustvault.entity.User;
import com.trustvault.entity.UserStatus;
import com.trustvault.exception.ResourceNotFoundException;
import com.trustvault.exception.ValidationException;
import com.trustvault.repository.UserRepository;
import com.trustvault.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DocumentAuthorizationService authorizationService;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       DocumentAuthorizationService authorizationService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(User currentUser) {
        authorizationService.checkAdmin(currentUser);
        return userRepository.findAllByOrderByEmailAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsersForSharing(String query, User currentUser) {
        authorizationService.checkUserActive(currentUser);
        String q = (query != null) ? query.trim() : "";
        List<User> users = q.isBlank()
                ? userRepository.findAllByOrderByEmailAsc()
                : userRepository.findByEmailContainingIgnoreCase(q);

        // Filter out current user and disabled/suspended users from sharing autocomplete
        return users.stream()
                .filter(u -> !u.getId().equals(currentUser.getId()) && u.getStatus() == UserStatus.ACTIVE)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserDto updateUserStatus(UUID targetUserId, UserStatus newStatus, User currentUser) {
        authorizationService.checkAdmin(currentUser);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (target.getId().equals(currentUser.getId())) {
            throw new ValidationException("Administrators cannot change their own status");
        }

        UserStatus oldStatus = target.getStatus();
        target.setStatus(newStatus);
        User saved = userRepository.save(target);

        auditService.recordEvent(
                currentUser.getId(),
                AuditAction.ADMIN_USER_UPDATE,
                null,
                saved.getId(),
                AuditOutcome.SUCCESS,
                SecurityUtils.getCorrelationId(),
                String.format("Changed status of user %s from %s to %s", saved.getEmail(), oldStatus, newStatus)
        );

        return toDto(saved);
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
