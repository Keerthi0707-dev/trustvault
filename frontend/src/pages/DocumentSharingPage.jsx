import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/api';
import ConfirmModal from '../components/ConfirmModal';

export default function DocumentSharingPage() {
  const { id } = useParams();

  const [document, setDocument] = useState(null);
  const [grants, setGrants] = useState([]);
  const [availableUsers, setAvailableUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // New grant form state
  const [recipientEmail, setRecipientEmail] = useState('');
  const [permission, setPermission] = useState('READ');
  const [granting, setGranting] = useState(false);
  const [grantError, setGrantError] = useState('');

  // Revocation modal state
  const [revokeTargetGrant, setRevokeTargetGrant] = useState(null);
  const [revoking, setRevoking] = useState(false);

  useEffect(() => {
    loadData();
  }, [id]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [docData, grantsData, usersData] = await Promise.all([
        api.getDocument(id),
        api.listGrants(id),
        api.searchUsers().catch(() => [])
      ]);
      setDocument(docData);
      setGrants(grantsData);
      setAvailableUsers(usersData);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load sharing permissions');
    } finally {
      setLoading(false);
    }
  };

  const handleGrantSubmit = async (e) => {
    e.preventDefault();
    if (!recipientEmail) {
      setGrantError('Please select or enter a recipient user');
      return;
    }

    setGrantError('');
    setGranting(true);

    try {
      await api.createGrant(id, recipientEmail, permission);
      setSuccessMsg(`Successfully granted ${permission} access to ${recipientEmail}`);
      setRecipientEmail('');
      const updatedGrants = await api.listGrants(id);
      setGrants(updatedGrants);
    } catch (err) {
      setGrantError(err.message || 'Failed to grant permission');
    } finally {
      setGranting(false);
    }
  };

  const handleRevokeConfirm = async () => {
    if (!revokeTargetGrant) return;
    setRevoking(true);
    try {
      await api.revokeGrant(id, revokeTargetGrant.id);
      setSuccessMsg(`Access for ${revokeTargetGrant.recipientEmail} has been revoked immediately.`);
      setRevokeTargetGrant(null);
      const updatedGrants = await api.listGrants(id);
      setGrants(updatedGrants);
    } catch (err) {
      alert(err.message || 'Failed to revoke access');
    } finally {
      setRevoking(false);
    }
  };

  const activeGrants = grants.filter((g) => g.active);
  const revokedGrants = grants.filter((g) => !g.active);

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>Loading access permissions...</div>;
  }

  if (error || !document) {
    return (
      <div style={{ maxWidth: '600px', margin: '40px auto' }}>
        <div className="glass-panel" style={{ textAlign: 'center', padding: '40px 24px' }}>
          <h2 style={{ fontSize: '20px', fontWeight: 600, marginBottom: '8px' }}>Sharing Unavailable</h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '24px', fontSize: '14px' }}>{error}</p>
          <Link to="/documents" className="btn btn-primary">Back to Documents</Link>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '20px' }}>
        <Link to={`/documents/${document.id}`} style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
          &larr; Back to {document.originalDisplayName}
        </Link>
      </div>

      <div style={{ marginBottom: '28px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '4px' }}>
          Permissions & Access Control
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          Manage who can view, download, or edit <strong>{document.originalDisplayName}</strong>
        </p>
      </div>

      {successMsg && <div className="alert alert-success">{successMsg}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      {/* Grant Access Form */}
      <div className="glass-panel" style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '14px' }}>Grant Document Access</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '20px' }}>
          Permissions are enforced server-side on every request. Grants can be immediately revoked at any time.
        </p>

        {grantError && <div className="alert alert-error">{grantError}</div>}

        <form onSubmit={handleGrantSubmit} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', alignItems: 'flex-end' }}>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Recipient User *</label>
            <input
              type="email"
              required
              className="form-control"
              list="usersList"
              placeholder="e.g. viewer@trustvault.local"
              value={recipientEmail}
              onChange={(e) => setRecipientEmail(e.target.value)}
            />
            <datalist id="usersList">
              {availableUsers.map((u) => (
                <option key={u.id} value={u.email}>
                  {u.email} ({u.role})
                </option>
              ))}
            </datalist>
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label">Permission Level *</label>
            <select
              className="form-control"
              value={permission}
              onChange={(e) => setPermission(e.target.value)}
            >
              <option value="READ">READ (View & Download Only)</option>
              <option value="EDIT">EDIT (View, Download & Edit Metadata)</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={granting}
            className="btn btn-primary"
            style={{ height: '42px' }}
          >
            {granting ? 'Granting...' : 'Grant Access'}
          </button>
        </form>
      </div>

      {/* Active Grants */}
      <div className="glass-panel" style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '14px' }}>Active Access Grants</h2>
        {activeGrants.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)', padding: '20px 0', fontSize: '14px' }}>
            No external users currently have access to this document. Only you (the owner) can access it.
          </p>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Recipient</th>
                  <th>Permission</th>
                  <th>Granted By</th>
                  <th>Granted Date</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {activeGrants.map((grant) => (
                  <tr key={grant.id}>
                    <td style={{ fontWeight: 500 }}>{grant.recipientEmail}</td>
                    <td>
                      <span className={`badge badge-${grant.permission.toLowerCase()}`}>
                        {grant.permission}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                      {grant.grantingUserEmail}
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                      {new Date(grant.createdAt).toLocaleString()}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        onClick={() => setRevokeTargetGrant(grant)}
                        className="btn btn-danger btn-sm"
                      >
                        Revoke Access
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Revocation History */}
      {revokedGrants.length > 0 && (
        <div className="glass-panel">
          <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '6px' }}>Revocation Audit History</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '16px' }}>
            Historical record of previously revoked grants. Revoked grants are preserved in the audit log and cannot authorize access.
          </p>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Recipient</th>
                  <th>Former Permission</th>
                  <th>Granted Date</th>
                  <th>Revoked Date</th>
                  <th>Revoked By</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {revokedGrants.map((grant) => (
                  <tr key={grant.id}>
                    <td style={{ color: 'var(--text-secondary)' }}>{grant.recipientEmail}</td>
                    <td>
                      <span className="badge badge-revoked">{grant.permission}</span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                      {new Date(grant.createdAt).toLocaleDateString()}
                    </td>
                    <td style={{ color: 'var(--accent-rose)', fontSize: '13px' }}>
                      {grant.revocationTimestamp ? new Date(grant.revocationTimestamp).toLocaleString() : '-'}
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                      {grant.revokingUserEmail || 'Owner'}
                    </td>
                    <td>
                      <span className="badge badge-danger">Revoked</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Revocation Confirmation Modal */}
      <ConfirmModal
        isOpen={!!revokeTargetGrant}
        title="Immediate Access Revocation"
        message={`Are you sure you want to revoke access for ${revokeTargetGrant?.recipientEmail}? The user's next download or metadata request will immediately fail with Access Denied.`}
        confirmText={revoking ? 'Revoking...' : 'Revoke Immediately'}
        confirmVariant="danger"
        onConfirm={handleRevokeConfirm}
        onCancel={() => setRevokeTargetGrant(null)}
      />
    </div>
  );
}
