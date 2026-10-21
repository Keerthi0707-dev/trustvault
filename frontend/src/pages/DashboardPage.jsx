import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';

export default function DashboardPage() {
  const { user } = useAuth();
  const [documents, setDocuments] = useState([]);
  const [activity, setActivity] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [docsData, actData] = await Promise.all([
        api.listDocuments(),
        api.getActivity().catch(() => [])
      ]);
      setDocuments(docsData);
      setActivity(actData.slice(0, 5));
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const ownedCount = documents.filter((d) => d.currentUserPermission === 'OWNER').length;
  const sharedCount = documents.filter((d) => d.currentUserPermission !== 'OWNER').length;

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>
        <p>Loading security dashboard...</p>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
        <div>
          <h1 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '4px' }}>Welcome back, {user?.email}</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Vault status: <span style={{ color: 'var(--accent-teal)', fontWeight: 600 }}>Active & Protected</span> (Role: {user?.role})
          </p>
        </div>
        {(user?.role === 'OWNER' || user?.role === 'ADMIN') && (
          <Link to="/documents?action=upload" className="btn btn-primary">
            + Upload Document
          </Link>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Metric Cards */}
      <div className="grid-cards" style={{ marginBottom: '32px' }}>
        <div className="glass-panel" style={{ borderLeft: '4px solid var(--accent-cyan)' }}>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>
            Accessible Documents
          </div>
          <div style={{ fontSize: '32px', fontWeight: 700 }}>{documents.length}</div>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Available in your vault
          </div>
        </div>

        <div className="glass-panel" style={{ borderLeft: '4px solid var(--accent-indigo)' }}>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>
            Owned by You
          </div>
          <div style={{ fontSize: '32px', fontWeight: 700 }}>{ownedCount}</div>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Full management & sharing
          </div>
        </div>

        <div className="glass-panel" style={{ borderLeft: '4px solid var(--accent-teal)' }}>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>
            Shared With You
          </div>
          <div style={{ fontSize: '32px', fontWeight: 700 }}>{sharedCount}</div>
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Read/Edit grants active
          </div>
        </div>
      </div>

      {/* Main Sections */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '28px' }}>
        {/* Recent Documents */}
        <div className="glass-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 600 }}>Recent Accessible Documents</h2>
            <Link to="/documents" style={{ fontSize: '13px' }}>View all documents &rarr;</Link>
          </div>

          {documents.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-secondary)' }}>
              <p style={{ marginBottom: '14px' }}>No documents in your vault yet.</p>
              {(user?.role === 'OWNER' || user?.role === 'ADMIN') && (
                <Link to="/documents?action=upload" className="btn btn-secondary btn-sm">
                  Upload your first document
                </Link>
              )}
            </div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Document Name</th>
                    <th>Category</th>
                    <th>Owner</th>
                    <th>Your Permission</th>
                    <th>Created</th>
                    <th style={{ textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {documents.slice(0, 6).map((doc) => (
                    <tr key={doc.id}>
                      <td style={{ fontWeight: 500 }}>
                        <Link to={`/documents/${doc.id}`}>{doc.originalDisplayName}</Link>
                      </td>
                      <td>
                        <span style={{ color: 'var(--text-secondary)' }}>{doc.category}</span>
                      </td>
                      <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                        {doc.ownerEmail}
                      </td>
                      <td>
                        <span className={`badge badge-${doc.currentUserPermission.toLowerCase()}`}>
                          {doc.currentUserPermission}
                        </span>
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                        {new Date(doc.createdAt).toLocaleDateString()}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '8px' }}>
                          <a
                            href={api.downloadDocumentUrl(doc.id)}
                            className="btn btn-secondary btn-sm"
                            download
                            title="Download verified document"
                          >
                            Download
                          </a>
                          <Link to={`/documents/${doc.id}`} className="btn btn-secondary btn-sm">
                            Details
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Recent Activity Feed */}
        {activity.length > 0 && (
          <div className="glass-panel">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h2 style={{ fontSize: '18px', fontWeight: 600 }}>Recent Security Activity</h2>
              <Link to="/activity" style={{ fontSize: '13px' }}>Full activity log &rarr;</Link>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {activity.map((act) => (
                <div
                  key={act.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '10px 14px',
                    background: 'rgba(30, 41, 59, 0.4)',
                    borderRadius: '8px',
                    fontSize: '13px'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <span
                      style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: act.outcome === 'SUCCESS' ? 'var(--accent-teal)' : 'var(--accent-rose)',
                      }}
                    />
                    <span style={{ fontWeight: 600 }}>{act.action}</span>
                    <span style={{ color: 'var(--text-secondary)' }}>{act.details}</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
                    {new Date(act.timestamp).toLocaleTimeString()}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
