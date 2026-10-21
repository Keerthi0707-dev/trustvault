package com.trustvault.service;

import com.trustvault.entity.*;
import com.trustvault.repository.DocumentGrantRepository;
import com.trustvault.repository.DocumentRepository;
import com.trustvault.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentGrantRepository documentGrantRepository;
    private final DocumentStorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final boolean seedEnabled;

    public DemoDataInitializer(UserRepository userRepository,
                               DocumentRepository documentRepository,
                               DocumentGrantRepository documentGrantRepository,
                               DocumentStorageService storageService,
                               PasswordEncoder passwordEncoder,
                               AuditService auditService,
                               @Value("${trustvault.seed-demo-users:true}") boolean seedEnabled) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentGrantRepository = documentGrantRepository;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Demo user seeding is disabled");
            return;
        }

        log.info("Checking and seeding demo security accounts...");

        User admin = seedUserIfNotExists("admin@trustvault.local", "AdminSecretPass123!", Role.ADMIN, UserStatus.ACTIVE);
        User owner = seedUserIfNotExists("owner@trustvault.local", "OwnerSecretPass123!", Role.OWNER, UserStatus.ACTIVE);
        User alice = seedUserIfNotExists("alice@trustvault.local", "AliceSecretPass123!", Role.OWNER, UserStatus.ACTIVE);
        User viewer = seedUserIfNotExists("viewer@trustvault.local", "ViewerSecretPass123!", Role.VIEWER, UserStatus.ACTIVE);
        User bob = seedUserIfNotExists("bob@trustvault.local", "BobSecretPass123!", Role.VIEWER, UserStatus.ACTIVE);
        seedUserIfNotExists("suspended@trustvault.local", "SuspendedPass123!", Role.VIEWER, UserStatus.SUSPENDED);
        seedUserIfNotExists("disabled@trustvault.local", "DisabledPass123!", Role.VIEWER, UserStatus.DISABLED);

        log.info("TrustVault demo user seeding completed successfully");
    }

    private User seedUserIfNotExists(String email, String rawPassword, Role role, UserStatus status) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User(
                    UUID.randomUUID(),
                    email,
                    passwordEncoder.encode(rawPassword),
                    role,
                    status
            );
            User saved = userRepository.save(user);
            auditService.recordEvent(
                    saved.getId(),
                    AuditAction.ADMIN_USER_UPDATE,
                    null,
                    saved.getId(),
                    AuditOutcome.SUCCESS,
                    "bootstrap",
                    "Seeded demo account with role " + role + " and status " + status
            );
            log.info("Seeded demo account: {} ({})", email, role);
            return saved;
        });
    }
}
