package com.trustvault.security;

import com.trustvault.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private SecurityUtils() {
    }

    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return Optional.of((CustomUserDetails) principal);
            }
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUserDetails().map(CustomUserDetails::getId);
    }

    public static Optional<User> getCurrentUser() {
        return getCurrentUserDetails().map(CustomUserDetails::getUser);
    }

    public static String getCorrelationId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId != null && !correlationId.isBlank()) {
                return correlationId;
            }
            Object attr = request.getAttribute("CORRELATION_ID");
            if (attr != null) {
                return attr.toString();
            }
            String newId = UUID.randomUUID().toString();
            request.setAttribute("CORRELATION_ID", newId);
            return newId;
        }
        return UUID.randomUUID().toString();
    }

    public static String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isBlank()) {
                return xfHeader.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "127.0.0.1";
    }
}
