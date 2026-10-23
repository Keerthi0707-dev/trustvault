# TrustVault — Secure Document Management Platform

TrustVault is a production-grade, secure document-sharing web application built for a high-stakes security challenge. It provides military-grade access control, server-mediated private storage, cryptographic tamper-evident audit logging, and single-origin packaging.

[![Build & Test](https://img.shields.io/badge/Security%20Tests-25%2F25%20Passing-brightgreen)](#automated-testing)
[![Architecture](https://img.shields.io/badge/Architecture-Single%20Deployable%20Artifact-blue)](#architecture)
[![Java](https://img.shields.io/badge/Java-21-orange)](#technology-stack)
[![React](https://img.shields.io/badge/React-19-cyan)](#technology-stack)

> **Challenge Deliverables & Documentation**:
> - 📄 **[WRITEUP.md](file:///c:/Users/keert/OneDrive/Desktop/TrustVault/WRITEUP.md)**: Architectural retrospective, deliberate non-features, permission model, AI errors, and gaps.
> - 🛡️ **[THREATS.md](file:///c:/Users/keert/OneDrive/Desktop/TrustVault/THREATS.md)**: Deep threat model (T-01 through T-10), mitigations, and automated verification.
> - 🤖 **[PROMPTS.md](file:///c:/Users/keert/OneDrive/Desktop/TrustVault/PROMPTS.md)**: Dated AI prompt interactions, hallucinations caught, and fixes with commit links.
> - 🔌 **[API.md](file:///c:/Users/keert/OneDrive/Desktop/TrustVault/API.md)**: Complete REST API specification with endpoint-level authorization rules.
> - 🎬 **[DEMO.md](file:///c:/Users/keert/OneDrive/Desktop/TrustVault/DEMO.md)**: Structured 5-minute evaluation demonstration script.

---

## 1. Architecture Overview

TrustVault follows a **single-service, single-origin architecture**:
- **Unified Web Application**: Spring Boot 3.3.5 serves both the REST API and the compiled React SPA from the same domain and origin, eliminating CORS vulnerabilities and simplifying deployment.
- **Server-Side Session Authentication**: State-of-the-art session management with session fixation migration, `HttpOnly` and `SameSite=Lax` cookies, and SPA CSRF double-submit token validation.
- **Centralized Authorization**: `DocumentAuthorizationService` independently validates every request against the authenticated identity, document ownership, active grants, and account status.
- **Private Storage Mediation**: Documents are stored in a private Supabase Storage bucket (`trustvault-documents`). The frontend never receives privileged credentials; every byte uploaded and downloaded is streamed through backend validation.
- **Tamper-Evident Audit Logging**: Every security-relevant action is recorded in a cryptographic SHA-256 hash chain with administrative verification capable of detecting unauthorized log modifications.

```
┌────────────────────────────────────────────────────────┐
│                   TrustVault Client                    │
│      React SPA (Single Origin, Relative API Calls)     │
└───────────────────────────┬────────────────────────────┘
                            │ HTTPS / Session Cookie + CSRF
┌───────────────────────────▼────────────────────────────┐
│              Render Web Service (Docker)               │
│  ┌──────────────────────────────────────────────────┐  │
│  │             Spring Boot Application              │  │
│  │  - Static Resource Forwarder (React App)         │  │
│  │  - SecurityConfig (Sessions, CSRF, RBAC)         │  │
│  │  - DocumentAuthorizationService (Strict Checks)  │  │
│  │  - AuditService (SHA-256 Hash Chaining)          │  │
│  │  - DocumentStorageService (Tika, Validation)     │  │
│  └───────────┬──────────────────────────┬───────────┘  │
└──────────────┼──────────────────────────┼──────────────┘
               │ TLS 5432 / 6543          │ HTTPS / Bearer
┌──────────────▼───────────┐  ┌───────────▼──────────────┐
│    Supabase PostgreSQL   │  │     Supabase Storage     │
│   (Users, Grants, Logs)  │  │   (trustvault-documents) │
└──────────────────────────┘  └──────────────────────────┘
```

---

## 2. Technology Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security 6 (Session-based auth, BCrypt 12 rounds, CSRF tokens)
- **Data Persistence**: Spring Data JPA & Hibernate
- **Database Migrations**: Flyway Core & Flyway PostgreSQL
- **File Inspection**: Apache Tika (MIME magic byte verification)
- **Cryptography**: BouncyCastle & Java MessageDigest SHA-256
- **Testing**: JUnit 5, Spring Security Test, MockMvc, H2 database

### Frontend
- **Framework**: React 19 (JavaScript ES6+)
- **Build Tool**: Vite 8
- **Routing**: React Router 7
- **Styling**: Vanilla CSS Design System with sleek dark security aesthetic and glassmorphism

### Storage & Database
- **Application Database**: Supabase PostgreSQL (Project `fjtegicvsdggajprxjdi`)
- **Document Storage**: Supabase Storage private bucket `trustvault-documents`

---

## 3. Demo Accounts & Role Hierarchy

The application includes pre-configured demonstration accounts for the challenge:

| Email | Password | Role | Description |
|---|---|---|---|
| `admin@trustvault.local` | `AdminSecretPass123!` | `ADMIN` | System administrator. Can manage user statuses and run audit integrity verification. Cannot view private documents without explicit grant. |
| `owner@trustvault.local` | `OwnerSecretPass123!` | `OWNER` | Document owner. Can upload, preview, download, edit metadata, delete, and manage document sharing grants. |
| `alice@trustvault.local` | `AliceSecretPass123!` | `OWNER` | Secondary owner. Useful for testing cross-user authorization boundaries. |
| `viewer@trustvault.local` | `ViewerSecretPass123!` | `VIEWER` | Grant recipient. Can only access documents explicitly shared with them. Cannot upload or delete. |
| `bob@trustvault.local` | `BobSecretPass123!` | `VIEWER` | Secondary viewer for verifying multi-party access control. |
| `suspended@trustvault.local` | `SuspendedPass123!` | `VIEWER` | Suspended account. Verifies that deactivated accounts are immediately barred. |

---

## 4. Key Security Controls

### Strict Authorization & IDOR Protection
- Document IDs are random UUIDs. Regardless of whether an ID is known or guessed, `DocumentAuthorizationService` verifies active ownership or grants.
- Unauthorized requests return generic **HTTP 404 Not Found** instead of 403 to prevent object enumeration.

### Immediate Revocation
- When an owner revokes a grant, the `revocation_timestamp` is populated in the database.
- Every subsequent request (including file downloads) re-verifies active grants on the database. Revocation takes effect instantaneously without waiting for session expiry or cache invalidation.

### Tamper-Evident SHA-256 Audit Trail
- Each audit record computes a cryptographic SHA-256 hash across canonical fields:
  `eventId | timestampMillis | actorId | action | docId | targetUserId | outcome | correlationId | details | prevHash`
- `AuditService.verifyAuditIntegrity()` mathematically walks the chain from genesis to head, immediately catching any modified payload or broken link.

### Content Inspection & Path Traversal Prevention
- Filenames are sanitized via regex `[^a-zA-Z0-9._-]` and stripped of directory traversal prefixes (`../`).
- Apache Tika inspects raw file magic bytes, blocking executable scripts, HTML, SVG with embedded scripts, and unauthorized binaries.

---

## 5. Local Setup & Execution

### Prerequisites
- Java 21 or newer
- Node.js 20+ and npm
- Maven 3.9+ (or use the included `./mvnw`)

### 1. Configure Local Environment
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Fill in your Supabase database URL and secret key if testing with live Supabase. If left blank, the application automatically runs in isolated offline test mode using an in-memory database and local vault storage.

### 2. Run Backend
```bash
cd backend
./mvnw spring-boot:run
```
Spring Boot starts on `http://localhost:8080`.

### 3. Run Frontend (Development Mode)
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. Vite proxies `/api` and `/actuator` requests to `http://localhost:8080`.

---

## 6. Automated Testing

Run the automated security and integration test suite:
```bash
cd backend
./mvnw test
```

### Verified Test Results (25 of 25 Passing)
- `AuditIntegrityTest.testValidAuditChainVerification`: PASSED
- `AuditIntegrityTest.testTamperDetectionOnPayloadModification`: PASSED
- `AuditIntegrityTest.testTamperDetectionOnBrokenChainLink`: PASSED
- `DocumentAuthorizationServiceTest.testSuspendedUserDenied`: PASSED
- `DocumentAuthorizationServiceTest.testViewerCannotUpload`: PASSED
- `DocumentAuthorizationServiceTest.testOwnerCanReadAndDownload`: PASSED
- `DocumentAuthorizationServiceTest.testUnauthorizedUserDeniedWithNotFound`: PASSED
- `DocumentAuthorizationServiceTest.testReadGrantPermissions`: PASSED
- `DocumentAuthorizationServiceTest.testEditGrantPermissions`: PASSED
- `DocumentAuthorizationServiceTest.testImmediateRevocationDenial`: PASSED
- `DocumentStorageValidationTest.testPathTraversalSanitization`: PASSED
- `DocumentStorageValidationTest.testDangerousExtensionsRejected`: PASSED
- `DocumentStorageValidationTest.testSha256Checksum`: PASSED
- `SecurityIntegrationTest.testHealthEndpoint`: PASSED
- `SecurityIntegrationTest.testAnonymousAccessDenied`: PASSED
- `SecurityIntegrationTest.testInvalidLoginFails`: PASSED
- `SecurityIntegrationTest.testValidLoginCreatesSession`: PASSED
- `SecurityIntegrationTest.testNonAdminCannotAccessAdminEndpoints`: PASSED
- `SecurityIntegrationTest.testAdminCanAccessAdminEndpoints`: PASSED
- `SecurityIntegrationTest.testEndToEndUploadSharingAndImmediateRevocation`: PASSED
- `SecurityIntegrationTest.testCsrfProtectionEnforcedForStateChangingRequests`: PASSED
- `AiAccessAdvisorAndSearchTest.testAiAdvisorGeneratesLeastPrivilegeSuggestionForOwner`: PASSED
- `AiAccessAdvisorAndSearchTest.testNonOwnerCannotRequestAiAccessSuggestion`: PASSED
- `AiAccessAdvisorAndSearchTest.testAiAssistedGrantCreationRecordsRationaleInAudit`: PASSED
- `AiAccessAdvisorAndSearchTest.testSearchDocumentsByDateRange`: PASSED

---

## 7. Production Packaging & Combined Build

TrustVault is packaged into a single executable JAR containing the production frontend:
```bash
# 1. Build React frontend
cd frontend
npm run build

# 2. Package Spring Boot executable JAR (automatically bundles frontend/dist)
cd ../backend
./mvnw clean package -DskipTests

# 3. Run the packaged artifact
java -jar target/trustvault-backend-1.0.0.jar
```
Visit `http://localhost:8080` to experience the single-origin application.

---

## 8. Deployment to Render

TrustVault is deployed as a single Web Service on Render using the multi-stage `Dockerfile`:

- **Live Public URL**: [https://trustvault-zjdo.onrender.com](https://trustvault-zjdo.onrender.com)
- **Health Endpoint**: [https://trustvault-zjdo.onrender.com/actuator/health](https://trustvault-zjdo.onrender.com/actuator/health) (Status: `UP`)

### Deployment Configuration:
1. **GitHub Repository**: Connected to `https://github.com/Keerthi0707-dev/trustvault` (`main` branch).
2. **Runtime**: **Docker** using root `./Dockerfile`.
3. **Environment Variables Configured in Render**:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require` (Supabase IPv4 Session Pooler)
   - `SPRING_DATASOURCE_USERNAME`: `postgres.fjtegicvsdggajprxjdi`
   - `SPRING_DATASOURCE_PASSWORD`: `<supabase-db-password>`
   - `SUPABASE_URL`: `https://fjtegicvsdggajprxjdi.supabase.co`
   - `SUPABASE_SECRET_KEY`: `<supabase-service-role-key>`
   - `SUPABASE_STORAGE_BUCKET`: `trustvault-documents`
   - `SESSION_COOKIE_SECURE`: `true`
4. **Verification**: Single-origin architecture verified. The browser uses relative `/api/...` endpoints and serves the React SPA from the same domain.

