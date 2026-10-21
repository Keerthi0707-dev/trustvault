package com.trustvault.controller;

import com.trustvault.dto.AuthRequest;
import com.trustvault.dto.AuthResponse;
import com.trustvault.entity.AuditAction;
import com.trustvault.entity.AuditOutcome;
import com.trustvault.entity.User;
import com.trustvault.entity.UserStatus;
import com.trustvault.exception.AccountSuspendedException;
import com.trustvault.repository.UserRepository;
import com.trustvault.security.CustomUserDetails;
import com.trustvault.security.SecurityUtils;
import com.trustvault.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        String correlationId = SecurityUtils.getCorrelationId();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(request.getEmail().trim());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();

            if (user.getStatus() != UserStatus.ACTIVE) {
                auditService.recordEvent(
                        user.getId(),
                        AuditAction.LOGIN_FAILURE,
                        null,
                        null,
                        AuditOutcome.DENIAL,
                        correlationId,
                        "Login denied: account is " + user.getStatus()
                );
                throw new AccountSuspendedException("Account is " + user.getStatus().name().toLowerCase());
            }

            // Establish secure session
            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(auth);
            SecurityContextHolder.setContext(sc);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            auditService.recordEvent(
                    user.getId(),
                    AuditAction.LOGIN_SUCCESS,
                    null,
                    null,
                    AuditOutcome.SUCCESS,
                    correlationId,
                    "User signed in successfully from " + SecurityUtils.getClientIp()
            );

            return ResponseEntity.ok(new AuthResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus()
            ));
        } catch (BadCredentialsException ex) {
            userOpt.ifPresent(u -> auditService.recordEvent(
                    u.getId(),
                    AuditAction.LOGIN_FAILURE,
                    null,
                    null,
                    AuditOutcome.DENIAL,
                    correlationId,
                    "Failed login attempt: bad credentials from " + SecurityUtils.getClientIp()
            ));
            if (userOpt.isEmpty()) {
                auditService.recordEvent(
                        null,
                        AuditAction.LOGIN_FAILURE,
                        null,
                        null,
                        AuditOutcome.DENIAL,
                        correlationId,
                        "Failed login attempt for non-existent account: " + request.getEmail().trim()
                );
            }
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        User user = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new BadCredentialsException("Not authenticated"));

        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        ));
    }
}
