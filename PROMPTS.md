# TrustVault Development & AI Engineering Log

This document records the actual AI prompt interactions, architectural decisions, and outcomes during the design, implementation, and verification of TrustVault.

---

### Prompt 1: Project Inspection & Foundational Architecture Design
- **Date**: 2026-10-21
- **Focus**: Environment Discovery, Technology Stack Verification, and Multi-Stage Single-Origin Packaging
- **Prompt Summary**:
  > "Inspect the existing repository at `Keerthi0707-dev/trustvault` and local development environment. Setup the Maven wrapper, configure the single deployable Docker architecture combining React Vite and Spring Boot 3, and establish the base directory structure."
- **Actions & Outcome**:
  - Inspected the repository and detected an empty local workspace.
  - Initialized Git on `main` branch with remote pointing to `https://github.com/Keerthi0707-dev/trustvault`.
  - Configured portable Maven 3.9.9 and generated Maven wrapper scripts (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`).
  - Created root `.gitignore`, `.dockerignore`, and `.env.example` with zero hardcoded secrets.
  - Configured multi-stage `Dockerfile` and Maven `resources:copy-resources` to bundle React production assets directly into Spring Boot static classpath resources.

---

### Prompt 2: Flyway Migrations & Database Design
- **Date**: 2026-10-21
- **Focus**: Database Schema, Foreign Key Constraints, and Performance Indexing
- **Prompt Summary**:
  > "Design the Flyway database schema for TrustVault covering users, documents, document_grants with revocation tracking, and audit_events with cryptographic hash links. Ensure cross-database compatibility across both PostgreSQL and H2."
- **Actions & Outcome**:
  - Implemented `V1__initial_schema.sql` defining UUID primary keys, role enums, user status, soft-deletion timestamps, and audit event hashes.
  - Refined grant indexing to composite index `(document_id, recipient_user_id, revocation_timestamp)` to maintain compatibility between PostgreSQL (Supabase) and H2 (isolated testing).
  - Configured Spring Boot `application.yml` with HikariCP, Flyway automatic migration, and dynamic database driver resolution.

---

### Prompt 3: Centralized Authorization Service & Immediate Revocation (Critical Focus)
- **Date**: 2026-10-21
- **Focus**: Server-Side Access Control, IDOR Mitigation, and Instant Grant Revocation
- **Prompt Summary**:
  > "Implement the centralized `DocumentAuthorizationService` enforcing strict server-side authorization. Ensure that viewers can only access documents explicitly shared with them, EDIT permissions cannot share or delete, and revoked grants immediately block access on the very next request."
- **Actions & Outcome**:
  - Created `DocumentAuthorizationService` with explicit checks: `checkUserActive`, `checkCanUpload`, `checkCanViewMetadata`, `checkCanReadOrDownload`, `checkCanEdit`, `checkCanDelete`, and `checkCanManageGrants`.
  - Implemented anti-enumeration protection: unauthorized metadata and download attempts return **HTTP 404 Not Found** instead of 403, preventing attackers from confirming document existence.
  - Built `DocumentGrantService` with immediate revocation timestamping; eliminated permission caching to ensure instant enforcement.

---

### Prompt 4: Cryptographic SHA-256 Audit Trail & Tamper Detection (Critical Focus)
- **Date**: 2026-10-21
- **Focus**: Tamper-Evident Hash Chaining, Concurrency Safety, and Mathematical Verification
- **Prompt Summary**:
  > "Build the tamper-evident `AuditService` that computes a SHA-256 hash chain over canonical representations of each security event. Include thread-safe serialized write locking and an administrative integrity verification function that detects modified payloads or broken links."
- **Actions & Outcome**:
  - Implemented `computeCanonicalString` using millisecond-precision epoch timestamps, actor IDs, actions, outcomes, correlation IDs, details, and previous event hashes.
  - Integrated `ReentrantLock` to serialize previous-hash lookup and insertion, preventing race conditions under concurrent requests.
  - Built `verifyAuditIntegrity()` which walks the chain from genesis to head, re-computes hashes, and flags the exact ID of any tampered record.
  - Documented realistic threat limitations regarding direct database tampering vs external checkpoints.

---

### Prompt 5: Automated Security Test Suite & Negative Authorization Proof
- **Date**: 2026-10-21
- **Focus**: Comprehensive Security Validation, MockMvc Testing, and Negative Proof
- **Prompt Summary**:
  > "Write automated security tests covering authentication, RBAC, IDOR protection, immediate revocation, file upload validation, and audit chain verification. Execute a negative authorization proof by temporarily bypassing a check to prove the test suite catches vulnerabilities."
- **Actions & Outcome**:
  - Created 21 automated tests across `AuditIntegrityTest`, `DocumentAuthorizationServiceTest`, `DocumentStorageValidationTest`, and `SecurityIntegrationTest`.
  - Verified 100% test pass rate across all security boundaries.
  - Performed negative authorization proof: temporarily commented out the grant verification in `canReadOrDownload`; re-ran the test suite and confirmed 3 explicit test failures (`AssertionError: Status expected:<404> but was:<200>`).
  - Restored protection and confirmed clean build success with all 21 tests passing.

---

### Prompt 6: Modern React SPA & Production Integration
- **Date**: 2026-10-21
- **Focus**: UI/UX Design System, Double-Submit CSRF Protection, and Single-Origin Packaging
- **Prompt Summary**:
  > "Build the React user interface using Vite, React Router, and Vanilla CSS with a modern security-themed aesthetic. Implement login with demo accounts, document dashboard, upload modal, sharing controls, activity feed, and admin integrity dashboard."
- **Actions & Outcome**:
  - Developed full React SPA in `frontend/src` featuring glassmorphism, responsive navigation, and real-time validation.
  - Implemented `api.js` client with automatic `XSRF-TOKEN` cookie extraction and `X-XSRF-TOKEN` request header injection.
  - Configured Spring Boot `WebMvcConfig` to serve `index.html` for client-side React routes while preserving 404 JSON responses for missing `/api/**` endpoints.
  - Built and verified production JAR containing the bundled frontend dist assets (`BOOT-INF/classes/static/`).
