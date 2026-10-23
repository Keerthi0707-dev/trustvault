# TrustVault Engineering Write-Up

**Candidate**: Senior Full-Stack Security Engineer  
**Target**: TrustVault Secure Document Sharing & Cryptographic Audit Platform  
**Deliverable**: Architecture, Security Trade-offs, and Engineering Retrospective  

---

## 1. What We Built

TrustVault is a production-grade secure document platform architected to solve the two catastrophic failure modes of corporate document sharing: **creeping/stale permissions** and **unprovable access history**.

Key subsystems implemented:
1. **Single-Origin Secure Architecture**: Spring Boot 3.3.5 and React 19 bundled into a unified deployable JAR. Eliminates Cross-Origin Resource Sharing (CORS) vulnerabilities, third-party cookie blocking, and origin spoofing by serving both the static SPA and `/api/**` from the same origin.
2. **Server-Side Session Authentication & Anti-CSRF**: Session fixation rotation (`migrateSession`), `HttpOnly`, `SameSite=Lax` cookies, and double-submit CSRF cookie/header verification (`X-XSRF-TOKEN`).
3. **Role-Based Access Control (RBAC)**:
   - `ADMIN`: Manages account lifecycles and performs cryptographic audit integrity verifications. Least-privilege enforced: administrators cannot view or download documents without an explicit owner grant.
   - `OWNER`: Full management of owned documents: upload, metadata edits, soft-deletion, and sharing grants.
   - `VIEWER`: Strictly restricted to viewing and downloading documents explicitly granted to them.
4. **Per-Document Granular Grants**: Named access with explicit levels: `READ` (view & download) and `EDIT` (view, download, and metadata edits).
5. **Immediate Access Revocation**: Dynamic database queries enforcing `revocation_timestamp IS NULL`. Taking access away takes effect immediately on the very next HTTP packet without waiting for session expiry or cache invalidation.
6. **Anti-Enumeration Defense**: Unauthorized document requests return **HTTP 404 Not Found** instead of 403, preventing attackers from discovering document existence through ID guessing.
7. **Tamper-Evident SHA-256 Cryptographic Audit Ledger**: Every upload, download, metadata edit, deletion, grant creation, revocation, failed login, and denied access attempt is recorded into a sequential hash chain (`current_hash = SHA256(canonicalPayload + prev_hash)`). Writing is serialized using a JVM `ReentrantLock` to prevent concurrent bifurcations. A verification endpoint mathematically recalculates the chain from genesis to tip, identifying the exact record ID of any altered payload or broken link.
8. **Secure Server-Mediated Storage**: Frontend never receives storage bucket credentials or direct URLs. All uploads and downloads stream through backend validation. Apache Tika inspects raw file magic bytes, blocking executable scripts (`.exe`, `.sh`, `.php`), HTML, and SVG scripts regardless of file extension.
9. **Multi-Criteria Vault Search**: Search accessible documents by filename, description, owner email, category, and date ranges (`startDate`, `endDate`).
10. **AI Access Advisor (Least-Privilege Sharing Assistant)**: Intelligent advisor that suggests sharing recipients and least-privilege `READ` permissions based on document classification. In strict adherence to security rules, the machine never grants access autonomously; human owner confirmation is enforced, and both the AI recommendation and owner decision are logged to the audit ledger.
11. **Automated Test Suite (25 Tests)**: Complete test coverage including negative proofs verifying IDOR rejection, immediate revocation, CSRF enforcement, path traversal rejection, audit tampering detection, and AI access boundaries.

---

## 2. What We Deliberately Did Not Build (and Why)

1. **Stateless JWTs or Long-Lived Pre-Signed URLs**:
   - *Why rejected*: Pre-signed URLs and stateless JWTs cannot be revoked immediately without adding distributed state (Redis token blocklists). If an employee is terminated or a contract access is revoked, an active pre-signed URL or valid JWT allows access until expiration. TrustVault mediates all downloads through `DocumentAuthorizationService`, ensuring immediate, instantaneous revocation.
2. **Autonomous AI Permission Execution**:
   - *Why rejected*: Autonomous AI agents that manage permissions introduce critical "confused deputy" and indirect prompt injection vulnerabilities (e.g. an uploaded document containing adversarial text instructing the model to widen access). TrustVault keeps the AI strictly advisory; a human owner must review and confirm every grant.
3. **Public / Anonymous Share Links**:
   - *Why rejected*: Anonymous links destroy forensic non-repudiation. When an anonymous link leaks, it is impossible to prove *who* opened the document. Every grant in TrustVault is tied to a verified, authenticated identity.
4. **Folder-Level Cascading Inheritance**:
   - *Why rejected*: As stated in the prompt, folder inheritance is the primary cause of permission creep ("Someone is given access to a folder to see one file, and can now see forty"). Per-document grants ensure zero accidental leakage.
5. **Hard Deletion of Records**:
   - *Why rejected*: Hard-deleting rows destroys audit trail referential integrity. Documents are soft-deleted (`is_deleted = true`), rendering them immediately inaccessible while maintaining audit history.

---

## 3. The Permission Model in Our Own Words

TrustVault enforces a **Dual-Layer Least-Privilege Permission Model**:

```
[Incoming Request]
       │
       ▼
[Layer 1: Identity & Role Gate]
  - Authenticated session required? (Yes, except /api/auth/login, /actuator/health)
  - Account status: ACTIVE? (If SUSPENDED or DISABLED -> HTTP 403 Forbidden)
  - Role capabilities:
      • ADMIN: System administration, account suspension, audit verification
      • OWNER: Document upload, own document management
      • VIEWER: Document consumption
       │
       ▼
[Layer 2: Document Ownership & Grant Gate (Central Choke Point)]
  - Is Document soft-deleted? -> HTTP 404
  - Is Current User the Document Owner? -> Full Access (Read, Download, Edit, Delete, Share)
  - Does Current User have an Active Grant (revocation_timestamp IS NULL)?
      • Permission == READ: Can View Metadata, Download File. (Cannot Edit, Delete, or Share)
      • Permission == EDIT: Can View Metadata, Download File, Update Title/Category/Desc. (Cannot Delete or Share)
  - Neither Owner nor Active Grant? -> HTTP 404 Not Found (Anti-Enumeration)
```

The authorization logic is concentrated in a single, thoroughly tested service (`DocumentAuthorizationService`). No controller or data repository makes ad-hoc authorization assumptions.

---

## 4. How We Used AI and Where It Was Wrong

AI was leveraged throughout the project for boilerplate scaffolding, test harness generation, and React component styling. However, critical security vulnerabilities and architectural flaws produced by the AI had to be actively caught and corrected:

1. **Anti-Enumeration (HTTP 403 vs 404)**: The AI initially generated standard `@PreAuthorize` annotations that returned HTTP 403 Forbidden for unauthorized documents. We caught that this leaked document existence via ID enumeration and restructured the service to return HTTP 404.
2. **Audit Hash Concurrency Race**: The AI suggested calculating previous hashes asynchronously. We caught that concurrent requests fetched identical parent hashes, creating orphaned branches in the cryptographic chain. We replaced this with a serialized JVM `ReentrantLock` and synchronous transaction propagation.
3. **Stateless JWT Revocation Flaw**: The AI recommended stateless JWTs for download authorization. We recognized that this violates immediate revocation (Standard #3) and enforced server-mediated streaming with dynamic database grant verification.
4. **PostgreSQL Parameter Inference & Connection Pooler Deadlock**: The AI generated JPQL queries with untyped null checks `(:param IS NULL)` and configured `SERIALIZABLE` isolation. While this worked in H2, PostgreSQL rejected the parameter typing and PgBouncer connection poolers rejected transaction isolation changes. We refactored to explicit typed queries and application-level concurrency control.
5. **Autonomous AI Escalation Risk**: When asked to implement an AI assistant, the AI generated autonomous permission granting. We strictly constrained the AI to an advisory-only role with mandatory human owner confirmation.

*Full details and commit references are documented in [PROMPTS.md](file:///c:/Users/keert/OneDrive/Desktop/TrustVault/PROMPTS.md).*

---

## 5. What is Unfinished & Known Gaps

Being completely transparent about system boundaries and architectural trade-offs:

1. **Database-Internal Hash Chain vs External Anchoring**:
   - *Current state*: Audit records form a cryptographic SHA-256 chain within PostgreSQL. Modifying any historical row breaks verification.
   - *Limitation*: An attacker with direct, unrestricted PostgreSQL superuser access could theoretically recalculate hashes from the point of tampering to the tip.
   - *Production mitigation*: In enterprise deployments, the tip hash should be periodically committed to an external immutable ledger (e.g. AWS CloudTrail, immutable S3 object lock, or a public transparency log) to provide out-of-band non-repudiation.
2. **Virus / Malware Sandbox Pipeline**:
   - *Current state*: Apache Tika inspects magic bytes and prevents executable scripts and dangerous extensions.
   - *Limitation*: Malicious macros within valid `.docx` or exploit payloads within valid `.pdf` files pass MIME inspection.
   - *Production mitigation*: Integrating an asynchronous ClamAV or sandbox scanning pipeline before releasing files for download.
3. **Client-Side Zero-Knowledge Encryption**:
   - *Current state*: Files are encrypted at rest in private Supabase Storage and transmitted over TLS. The server mediates all decryption.
   - *Limitation*: A compromised server host could read document contents in transit. Implementing WebCrypto client-side envelope encryption would enable zero-knowledge vaulting.
