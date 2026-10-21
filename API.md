# TrustVault REST API Specification

**Base URL**: `/api`  
**Authentication**: Server-side session cookie (`TRUSTVAULT_SESSION`)  
**CSRF Defense**: Required header `X-XSRF-TOKEN` matching `XSRF-TOKEN` cookie on POST, PATCH, PUT, and DELETE  
**Error Format**: Consistent JSON response without stack traces or sensitive internals  

---

## 1. Authentication Endpoints

### 1.1 Sign In
- **Route**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "email": "owner@trustvault.local",
  "password": "OwnerSecretPass123!"
}
```
- **Response (200 OK)**:
```json
{
  "id": "a76ffa63-b78e-4648-85c3-21f7bd2d74aa",
  "email": "owner@trustvault.local",
  "role": "OWNER",
  "status": "ACTIVE"
}
```
- **Session Header**: `Set-Cookie: TRUSTVAULT_SESSION=...; Path=/; HttpOnly; SameSite=Lax`
- **Error Responses**:
  - `401 Unauthorized`: Bad credentials (`{"status":401,"error":"Unauthorized","message":"Invalid email or password"}`)
  - `403 Forbidden`: Account is suspended or disabled

### 1.2 Sign Out
- **Route**: `POST /api/auth/logout`
- **Access**: Authenticated
- **Response (200 OK)**: Clears `TRUSTVAULT_SESSION` and invalidates server-side session.

### 1.3 Current User Session
- **Route**: `GET /api/auth/me`
- **Access**: Authenticated
- **Response (200 OK)**: Returns the current user profile.

---

## 2. User Lookup

### 2.1 Search Users for Sharing
- **Route**: `GET /api/users?search={query}`
- **Access**: Authenticated (Active users only)
- **Response (200 OK)**:
```json
[
  {
    "id": "da9e8273-08e0-4ba9-a3fd-e61fda8398f2",
    "email": "viewer@trustvault.local",
    "role": "VIEWER",
    "status": "ACTIVE",
    "createdAt": "2026-10-21T18:00:00Z"
  }
]
```
*Note: Excludes current user and deactivated accounts to avoid invalid sharing grants.*

---

## 3. Document Endpoints

### 3.1 List / Search Accessible Documents
- **Route**: `GET /api/documents?search={query}&category={category}`
- **Access**: Authenticated
- **Response (200 OK)**:
```json
[
  {
    "id": "8704629b-3ec8-4770-963d-488074a45dd5",
    "originalDisplayName": "Q3_Financial_Audit.pdf",
    "mimeType": "application/pdf",
    "fileSize": 1048576,
    "category": "Finance",
    "description": "Internal Q3 audit assessment",
    "sha256Checksum": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "ownerId": "a76ffa63-b78e-4648-85c3-21f7bd2d74aa",
    "ownerEmail": "owner@trustvault.local",
    "createdAt": "2026-10-21T18:30:00Z",
    "updatedAt": "2026-10-21T18:30:00Z",
    "currentUserPermission": "OWNER"
  }
]
```

### 3.2 Upload Document
- **Route**: `POST /api/documents`
- **Access**: `OWNER` or `ADMIN`
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `file`: Multipart file (max 25MB, allowed extensions: pdf, docx, xlsx, pptx, txt, csv, md, png, jpg)
  - `category`: String (default: "General")
  - `description`: Optional text
- **Response (200 OK)**: Returns the created `DocumentDto`.
- **Error Responses**:
  - `400 Bad Request`: Disallowed extension, executable script signature, or invalid file.
  - `413 Payload Too Large`: File exceeds 25MB limit.

### 3.3 Get Document Metadata
- **Route**: `GET /api/documents/{id}`
- **Access**: Owner or active grant recipient
- **Response (200 OK)**: Returns `DocumentDto` with `currentUserPermission`.
- **Response (404 Not Found)**: Returned if the document does not exist OR current user is unauthorized (anti-enumeration).

### 3.4 Update Document Metadata
- **Route**: `PATCH /api/documents/{id}`
- **Access**: Owner or active `EDIT` grant recipient
- **Request Body**:
```json
{
  "originalDisplayName": "Updated_Title.pdf",
  "category": "Legal",
  "description": "Updated document description"
}
```
- **Response (200 OK)**: Returns updated `DocumentDto`.
- **Response (403 Forbidden)**: Returned if user holds a `READ` grant (insufficient permission).

### 3.5 Delete Document
- **Route**: `DELETE /api/documents/{id}`
- **Access**: Document Owner Only
- **Response (200 OK)**: `{"message":"Document successfully deleted"}`
- **Behavior**: Marks document soft-deleted (`isDeleted: true`); immediately revokes access for all grant recipients.

### 3.6 Download Document Content
- **Route**: `GET /api/documents/{id}/download`
- **Access**: Owner or active grant recipient (`READ` or `EDIT`)
- **Response (200 OK)**: Binary file stream with security headers:
  - `Content-Disposition: attachment; filename="..."`
  - `X-Content-Type-Options: nosniff`
  - `Content-Security-Policy: default-src 'none'`
  - `Cache-Control: no-store, no-cache, must-revalidate, private`
- **Response (404 Not Found)**: Inaccessible or revoked document.

### 3.7 Document Activity History
- **Route**: `GET /api/documents/{id}/audit`
- **Access**: Document Owner or Admin
- **Response (200 OK)**: Array of audit events for this specific document.

---

## 4. Permissions & Sharing Grants

### 4.1 List Grants for Document
- **Route**: `GET /api/documents/{id}/grants`
- **Access**: Document Owner Only
- **Response (200 OK)**:
```json
[
  {
    "id": "75963d40-435b-437e-ab37-6a03c80a6fec",
    "documentId": "8704629b-3ec8-4770-963d-488074a45dd5",
    "recipientUserId": "da9e8273-08e0-4ba9-a3fd-e61fda8398f2",
    "recipientEmail": "viewer@trustvault.local",
    "permission": "READ",
    "grantingUserId": "a76ffa63-b78e-4648-85c3-21f7bd2d74aa",
    "grantingUserEmail": "owner@trustvault.local",
    "createdAt": "2026-10-21T18:40:00Z",
    "revocationTimestamp": null,
    "revokingUserEmail": null,
    "active": true
  }
]
```

### 4.2 Create Grant
- **Route**: `POST /api/documents/{id}/grants`
- **Access**: Document Owner Only
- **Request Body**:
```json
{
  "recipientEmail": "viewer@trustvault.local",
  "permission": "READ"
}
```
- **Response (200 OK)**: Returns the created `GrantDto`.

### 4.3 Revoke Grant Immediately
- **Route**: `DELETE /api/documents/{id}/grants/{grantId}`
- **Access**: Document Owner Only
- **Response (200 OK)**: `{"message":"Permission grant revoked immediately"}`
- **Security Impact**: Takes effect instantaneously on the server; the recipient's next request is immediately rejected.

---

## 5. Security & Administrative Endpoints

### 5.1 System Health
- **Route**: `GET /actuator/health`
- **Access**: Public
- **Response (200 OK)**: `{"status":"UP"}`

### 5.2 User Management
- **Route**: `GET /api/admin/users`
- **Access**: `ROLE_ADMIN`
- **Response (200 OK)**: List of all users and account statuses.

### 5.3 Update User Status
- **Route**: `PATCH /api/admin/users/{id}/status`
- **Access**: `ROLE_ADMIN`
- **Request Body**: `{"status": "SUSPENDED"}` (Options: `ACTIVE`, `SUSPENDED`, `DISABLED`)
- **Response (200 OK)**: Returns updated user record.

### 5.4 Global Security Audit Trail
- **Route**: `GET /api/admin/audit`
- **Access**: `ROLE_ADMIN`
- **Response (200 OK)**: Full system audit ledger with actor emails, correlation IDs, and hash links.

### 5.5 Audit Integrity Verification
- **Route**: `GET /api/admin/audit/integrity`
- **Access**: `ROLE_ADMIN`
- **Response (200 OK)**:
```json
{
  "valid": true,
  "totalEventsChecked": 24,
  "firstCorruptedEventId": null,
  "statusMessage": "Cryptographic audit chain verified successfully (24 events)",
  "verifiedAt": "2026-10-21T19:20:00Z"
}
```
