import React, { useEffect, useState } from 'react';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';
import ConfirmModal from '../components/ConfirmModal';

export default function AdminPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Status update modal state
  const [targetUser, setTargetUser] = useState(null);
  const [newStatus, setNewStatus] = useState('');
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await api.getAdminUsers();
      setUsers(data);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load user list. Admin role required.');
    } finally {
      setLoading(false);
    }
  };

  const openStatusModal = (u, status) => {
    setTargetUser(u);
    setNewStatus(status);
  };

  const handleStatusConfirm = async () => {
    if (!targetUser || !newStatus) return;
    setUpdating(true);
    try {
      await api.updateUserStatus(targetUser.id, newStatus);
      setSuccessMsg(`Status for ${targetUser.email} changed to ${newStatus}`);
      setTargetUser(null);
      loadUsers();
    } catch (err) {
      alert(err.message || 'Failed to update user status');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '28px' }}>
        <h1 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '4px' }}>
          Administration: User & Access Management
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          Manage user accounts, assign security statuses, and prevent compromised account access
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMsg && <div className="alert alert-success">{successMsg}</div>}

      <div className="glass-panel" style={{ marginBottom: '24px' }}>
        <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
          <div style={{ fontSize: '24px' }}>🛡️</div>
          <div>
            <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>Principle of Least Privilege</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              Administrator status grants control over platform user provisioning and cryptographic audit integrity verification,
              but does not grant automatic access to private document contents without an explicit grant.
            </p>
          </div>
        </div>
      </div>

      <div className="glass-panel">
        <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '16px' }}>Registered Accounts</h2>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            Loading users...
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Email Address</th>
                  <th>Role</th>
                  <th>Current Status</th>
                  <th>Created Date</th>
                  <th style={{ textAlign: 'right' }}>Status Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => {
                  const isCurrent = u.id === user?.id;
                  return (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 500 }}>
                        {u.email} {isCurrent && <span style={{ color: 'var(--accent-cyan)', fontSize: '12px' }}>(You)</span>}
                      </td>
                      <td>
                        <span className={`badge badge-${u.role.toLowerCase()}`}>{u.role}</span>
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            u.status === 'ACTIVE'
                              ? 'badge-read'
                              : u.status === 'SUSPENDED'
                              ? 'badge-admin'
                              : 'badge-danger'
                          }`}
                        >
                          {u.status}
                        </span>
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                        {new Date(u.createdAt).toLocaleDateString()}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        {!isCurrent && (
                          <div style={{ display: 'inline-flex', gap: '6px' }}>
                            {u.status !== 'ACTIVE' && (
                              <button
                                onClick={() => openStatusModal(u, 'ACTIVE')}
                                className="btn btn-secondary btn-sm"
                              >
                                Activate
                              </button>
                            )}
                            {u.status !== 'SUSPENDED' && (
                              <button
                                onClick={() => openStatusModal(u, 'SUSPENDED')}
                                className="btn btn-secondary btn-sm"
                              >
                                Suspend
                              </button>
                            )}
                            {u.status !== 'DISABLED' && (
                              <button
                                onClick={() => openStatusModal(u, 'DISABLED')}
                                className="btn btn-danger btn-sm"
                              >
                                Disable
                              </button>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={!!targetUser}
        title="Change Account Security Status"
        message={`Are you sure you want to change ${targetUser?.email}'s status to ${newStatus}? If suspended or disabled, the account will be immediately barred from accessing documents.`}
        confirmText={updating ? 'Updating...' : `Set to ${newStatus}`}
        confirmVariant={newStatus === 'ACTIVE' ? 'success' : 'danger'}
        onConfirm={handleStatusConfirm}
        onCancel={() => setTargetUser(null)}
      />
    </div>
  );
}
