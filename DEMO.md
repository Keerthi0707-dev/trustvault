# TrustVault 5-Minute Live Demonstration Script

This structured script guides evaluators through verifying all core security, access control, cryptographic integrity, and single-origin features in approximately 5 minutes.

---

## Prerequisites
- Target URL: `http://localhost:8080` (or your live Render public URL).
- Browser: Chrome, Firefox, Safari, or Edge.

---

## Scenario 1: Authentication & Role-Based Dashboard (0:00 - 0:45)
1. Navigate to `/login`.
2. Notice the sleek security theme and the **Quick Demo Accounts** buttons.
3. Click **Document Owner** (`owner@trustvault.local` / `OwnerSecretPass123!`).
4. Click **Sign In**.
5. Observe the Dashboard:
   - User identity and role badge: `OWNER`.
   - Metric cards: Accessible Documents, Owned Documents, Shared Documents.
   - Recent activity stream.

---

## Scenario 2: Secure Document Upload & SHA-256 Checksum (0:45 - 1:30)
1. Click **+ Upload Document** from the navigation bar or dashboard.
2. Select any local test file (e.g. a `.pdf`, `.docx`, or `.txt` file).
3. Set Category to **Confidential** and enter Description: `High-priority financial security report`.
4. Click **Upload & Encrypt**.
5. The document appears in the vault list with its original display name and category.
6. Click **Details** on the newly uploaded document:
   - Observe the server-calculated **SHA-256 Checksum**.
   - Click **Copy SHA-256 Checksum** to copy the fingerprint.
   - Click **Download Document** to verify that the file streams smoothly through the backend.

---

## Scenario 3: IDOR Protection & Access Denial (1:30 - 2:30)
1. Copy the **Document ID** (UUID) from the document details URL (e.g., `/documents/3fa85f64-5717-4562-b3fc-2c963f66afa6`).
2. Click **Sign out** in the top navigation bar.
3. On the login page, click **Viewer (Recipient)** (`viewer@trustvault.local` / `ViewerSecretPass123!`) and sign in.
4. Notice the Viewer dashboard: the Owner's private document does NOT appear in the list.
5. In the browser address bar, paste the URL of the Owner's private document:
   `http://localhost:8080/documents/{copied-uuid}`
6. **Expected Security Behavior**:
   - The application displays **Access Denied or Not Found**.
   - The backend returned HTTP 404, preventing the viewer from confirming whether the private document exists (anti-enumeration).
   - An `ACCESS_DENIED` audit event is logged on the server.

---

## Scenario 4: Granular Sharing & Access Grant (2:30 - 3:15)
1. Sign out of the Viewer account.
2. Sign back in as **Document Owner** (`owner@trustvault.local`).
3. Open the confidential document and click **Manage Access** (or `/documents/{id}/sharing`).
4. In the **Grant Document Access** form:
   - Select or type `viewer@trustvault.local`.
   - Select Permission: **READ (View & Download Only)**.
   - Click **Grant Access**.
5. The grant appears in the **Active Access Grants** table.
6. Sign out and sign back in as **Viewer** (`viewer@trustvault.local`).
7. The document now appears in the Viewer's accessible document list with a green `READ` badge!
8. Click **Details** and **Download Document** — the download succeeds!
9. Notice that the Viewer has no **Edit** or **Delete** buttons, confirming strict RBAC boundaries.

---

## Scenario 5: Immediate Revocation Proof (3:15 - 4:00)
1. In a second browser tab (or after signing back in as **Owner**), go to the document's **Sharing** screen.
2. Next to `viewer@trustvault.local`, click **Revoke Access**.
3. Confirm in the dialog: "Revoke Immediately".
4. The grant immediately moves to the **Revocation Audit History** table.
5. Switch back to the Viewer's session and attempt to click **Download Document** or refresh the details page.
6. **Expected Security Behavior**:
   - The request immediately fails with **Access Denied / Not Found**.
   - No cached permissions or stale URLs permitted access. Immediate revocation is 100% verified.

---

## Scenario 6: Cryptographic Audit Hash Chain & Integrity Verification (4:00 - 4:45)
1. Sign out and sign in as **Security Admin** (`admin@trustvault.local` / `AdminSecretPass123!`).
2. Click **Audit & Integrity** in the navigation bar (`/admin/audit`).
3. Inspect the **Global Event Ledger**:
   - Every single action performed during the demo (logins, uploads, downloads, grants, revocations, and access denials) is recorded.
   - Note the **Current Event Hash** column showing the SHA-256 hash chaining.
4. Click **Verify Cryptographic Integrity**:
   - The backend mathematically verifies the entire chain from genesis to head.
   - A green banner confirms: `✓ HASH CHAIN INTEGRITY VERIFIED (Checked N chained events)`.

---

## Scenario 7: Account Suspension Enforcement (4:45 - 5:00)
1. Go to **Users** in the admin navigation (`/admin`).
2. Next to `viewer@trustvault.local`, click **Suspend**.
3. Confirm status change to `SUSPENDED`.
4. Sign out and attempt to sign in with `viewer@trustvault.local`.
5. **Expected Security Behavior**:
   - Login is denied with message: `Account is suspended`.
   - Deactivated accounts are barred from authentication and document retrieval.
