package com.trustvault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustvault.dto.AuthRequest;
import com.trustvault.entity.Role;
import com.trustvault.entity.User;
import com.trustvault.entity.UserStatus;
import com.trustvault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        if (!userRepository.existsByEmailIgnoreCase("admin@trustvault.local")) {
            userRepository.save(new User(
                    UUID.randomUUID(),
                    "admin@trustvault.local",
                    passwordEncoder.encode("AdminSecretPass123!"),
                    Role.ADMIN,
                    UserStatus.ACTIVE
            ));
        }
        if (!userRepository.existsByEmailIgnoreCase("owner@trustvault.local")) {
            userRepository.save(new User(
                    UUID.randomUUID(),
                    "owner@trustvault.local",
                    passwordEncoder.encode("OwnerSecretPass123!"),
                    Role.OWNER,
                    UserStatus.ACTIVE
            ));
        }
    }

    @Test
    @DisplayName("Health endpoint is publicly available")
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Anonymous access to protected API returns 401 Unauthorized")
    public void testAnonymousAccessDenied() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Login with invalid credentials returns 401 Unauthorized")
    public void testInvalidLoginFails() throws Exception {
        AuthRequest badReq = new AuthRequest("owner@trustvault.local", "WrongPassword999!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with valid credentials establishes authenticated session")
    public void testValidLoginCreatesSession() throws Exception {
        AuthRequest req = new AuthRequest("owner@trustvault.local", "OwnerSecretPass123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("owner@trustvault.local"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session, "Session must be created on successful login");

        // Authenticated request with session to /api/auth/me succeeds
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("owner@trustvault.local"));
    }

    @Test
    @DisplayName("Non-admin user cannot access admin audit endpoints (403 Forbidden)")
    public void testNonAdminCannotAccessAdminEndpoints() throws Exception {
        AuthRequest ownerReq = new AuthRequest("owner@trustvault.local", "OwnerSecretPass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession ownerSession = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(get("/api/admin/audit").session(ownerSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/audit/integrity").session(ownerSession))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin user can access admin audit and integrity endpoints")
    public void testAdminCanAccessAdminEndpoints() throws Exception {
        AuthRequest adminReq = new AuthRequest("admin@trustvault.local", "AdminSecretPass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession adminSession = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(get("/api/admin/audit").session(adminSession))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit/integrity").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").isBoolean());
    }

    @Test
    @DisplayName("End-to-End: Upload document, verify IDOR denial, grant read access, verify download, revoke access and verify immediate denial")
    public void testEndToEndUploadSharingAndImmediateRevocation() throws Exception {
        // Ensure viewer user exists
        if (!userRepository.existsByEmailIgnoreCase("viewer@trustvault.local")) {
            userRepository.save(new User(
                    UUID.randomUUID(),
                    "viewer@trustvault.local",
                    passwordEncoder.encode("ViewerSecretPass123!"),
                    Role.VIEWER,
                    UserStatus.ACTIVE
            ));
        }

        // 1. Owner logs in
        AuthRequest ownerReq = new AuthRequest("owner@trustvault.local", "OwnerSecretPass123!");
        MvcResult ownerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession ownerSession = (MockHttpSession) ownerLogin.getRequest().getSession();

        // 2. Viewer logs in
        AuthRequest viewerReq = new AuthRequest("viewer@trustvault.local", "ViewerSecretPass123!");
        MvcResult viewerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(viewerReq)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession viewerSession = (MockHttpSession) viewerLogin.getRequest().getSession();

        // 3. Owner uploads a confidential document
        org.springframework.mock.web.MockMultipartFile mockFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "confidential_contract.pdf", "application/pdf", "%PDF-1.4 confidential content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents")
                        .file(mockFile)
                        .param("category", "Legal")
                        .param("description", "Contract agreement")
                        .with(csrf())
                        .session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalDisplayName").value("confidential_contract.pdf"))
                .andReturn();

        String docIdStr = com.jayway.jsonpath.JsonPath.read(uploadResult.getResponse().getContentAsString(), "$.id");
        UUID docId = UUID.fromString(docIdStr);

        // 4. IDOR Protection: Viewer attempts to access document without grant -> 404 Not Found
        mockMvc.perform(get("/api/documents/" + docId).session(viewerSession))
                .andExpect(status().isNotFound());

        // Viewer attempts to download directly -> 404 Not Found
        mockMvc.perform(get("/api/documents/" + docId + "/download").session(viewerSession))
                .andExpect(status().isNotFound());

        // 5. Owner grants READ permission to viewer
        com.trustvault.dto.CreateGrantRequest grantReq = new com.trustvault.dto.CreateGrantRequest(
                "viewer@trustvault.local", com.trustvault.entity.GrantPermission.READ
        );

        MvcResult grantResult = mockMvc.perform(post("/api/documents/" + docId + "/grants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(grantReq))
                        .with(csrf())
                        .session(ownerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientEmail").value("viewer@trustvault.local"))
                .andExpect(jsonPath("$.permission").value("READ"))
                .andReturn();

        String grantIdStr = com.jayway.jsonpath.JsonPath.read(grantResult.getResponse().getContentAsString(), "$.id");
        UUID grantId = UUID.fromString(grantIdStr);

        // 6. Viewer now accesses metadata and downloads successfully
        mockMvc.perform(get("/api/documents/" + docId).session(viewerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUserPermission").value("READ"));

        mockMvc.perform(get("/api/documents/" + docId + "/download").session(viewerSession))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "no-store, no-cache, must-revalidate, private"));

        // Viewer attempts to edit metadata -> 403 Forbidden (READ cannot edit)
        com.trustvault.dto.DocumentUpdateRequest updateReq = new com.trustvault.dto.DocumentUpdateRequest(
                "tampered_name.pdf", "Legal", "Unauthorized edit"
        );
        mockMvc.perform(patch("/api/documents/" + docId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq))
                        .with(csrf())
                        .session(viewerSession))
                .andExpect(status().isForbidden());

        // 7. Owner revokes viewer's grant
        mockMvc.perform(delete("/api/documents/" + docId + "/grants/" + grantId)
                        .with(csrf())
                        .session(ownerSession))
                .andExpect(status().isOk());

        // 8. IMMEDIATE REVOCATION: The next request by viewer must immediately fail!
        mockMvc.perform(get("/api/documents/" + docId).session(viewerSession))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/documents/" + docId + "/download").session(viewerSession))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CSRF protection: state-changing session requests without CSRF token are rejected")
    public void testCsrfProtectionEnforcedForStateChangingRequests() throws Exception {
        AuthRequest ownerReq = new AuthRequest("owner@trustvault.local", "OwnerSecretPass123!");
        MvcResult ownerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession ownerSession = (MockHttpSession) ownerLogin.getRequest().getSession();

        // Making a POST request without CSRF token must result in 403 Forbidden
        mockMvc.perform(post("/api/documents/" + UUID.randomUUID() + "/grants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientEmail\":\"test@test.local\",\"permission\":\"READ\"}")
                        .session(ownerSession))
                .andExpect(status().isForbidden());
    }
}
