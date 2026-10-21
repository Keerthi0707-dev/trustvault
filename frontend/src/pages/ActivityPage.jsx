import React, { useEffect, useState } from 'react';
import { api } from '../api/api';

export default function ActivityPage() {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadActivities();
  }, []);

  const loadActivities = async () => {
    try {
      setLoading(true);
      const data = await api.getActivity();
      setActivities(data);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to retrieve activity log');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '28px' }}>
        <h1 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '4px' }}>Security Activity Log</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          Real-time tamper-evident record of actions and access events concerning your account and documents
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="glass-panel">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            Loading audit records...
          </div>
        ) : activities.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            No recent activity recorded.
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Actor</th>
                  <th>Action</th>
                  <th>Outcome</th>
                  <th>Correlation ID</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {activities.map((act) => (
                  <tr key={act.id}>
                    <td style={{ color: 'var(--text-muted)', fontSize: '12px', whiteSpace: 'nowrap' }}>
                      {new Date(act.timestamp).toLocaleString()}
                    </td>
                    <td style={{ fontSize: '13px' }}>{act.actorEmail || 'System'}</td>
                    <td style={{ fontWeight: 600 }}>{act.action}</td>
                    <td>
                      <span className={`badge ${act.outcome === 'SUCCESS' ? 'badge-read' : 'badge-danger'}`}>
                        {act.outcome}
                      </span>
                    </td>
                    <td style={{ fontSize: '12px' }}>
                      <span className="hash-code">{act.correlationId ? act.correlationId.slice(0, 8) + '...' : '-'}</span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                      {act.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
