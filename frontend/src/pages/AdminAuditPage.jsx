import React, { useEffect, useState } from 'react';
import { api } from '../api/api';

export default function AdminAuditPage() {
  const [auditEvents, setAuditEvents] = useState([]);
  const [integrityReport, setIntegrityReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [checkingIntegrity, setCheckingIntegrity] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadAuditData();
  }, []);

  const loadAuditData = async () => {
    try {
      setLoading(true);
      const [eventsData, reportData] = await Promise.all([
        api.getAdminAudit(),
        api.checkAuditIntegrity().catch(() => null)
      ]);
      setAuditEvents(eventsData);
      setIntegrityReport(reportData);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to retrieve administrative audit data');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyIntegrity = async () => {
    setCheckingIntegrity(true);
    try {
      const report = await api.checkAuditIntegrity();
      setIntegrityReport(report);
    } catch (err) {
      alert(err.message || 'Verification check failed');
    } finally {
      setCheckingIntegrity(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '4px' }}>
            Tamper-Evident Security Audit Trail
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Cryptographically chained SHA-256 event log with automated mathematical integrity verification
          </p>
        </div>

        <button
          onClick={handleVerifyIntegrity}
          disabled={checkingIntegrity}
          className="btn btn-primary"
        >
          {checkingIntegrity ? 'Verifying Chain...' : 'Verify Cryptographic Integrity'}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Integrity Report Card */}
      {integrityReport && (
        <div
          className="glass-panel"
          style={{
            marginBottom: '28px',
            borderLeft: `4px solid ${integrityReport.valid ? 'var(--accent-teal)' : 'var(--accent-rose)'}`
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
                <span
                  className={`badge ${integrityReport.valid ? 'badge-read' : 'badge-danger'}`}
                  style={{ fontSize: '13px', padding: '5px 12px' }}
                >
                  {integrityReport.valid ? '✓ HASH CHAIN INTEGRITY VERIFIED' : '⚠ INTEGRITY VIOLATION DETECTED'}
                </span>
                <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
                  Checked {integrityReport.totalEventsChecked} chained events
                </span>
              </div>
              <p style={{ fontSize: '14px', color: integrityReport.valid ? 'var(--text-primary)' : 'var(--rose-text)', fontWeight: integrityReport.valid ? 500 : 700 }}>
                {integrityReport.statusMessage}
              </p>
              {integrityReport.firstCorruptedEventId && (
                <p style={{ fontSize: '13px', color: 'var(--rose-text)', marginTop: '6px' }}>
                  Corrupted event ID: <span className="hash-code">{integrityReport.firstCorruptedEventId}</span>
                </p>
              )}
            </div>

            <div style={{ fontSize: '12px', color: 'var(--text-muted)', textAlign: 'right' }}>
              <div>Verified At</div>
              <div>{new Date(integrityReport.verifiedAt).toLocaleTimeString()}</div>
            </div>
          </div>
        </div>
      )}

      {/* Audit Limitation Disclosure */}
      <div className="glass-panel" style={{ padding: '16px 20px', marginBottom: '24px', background: 'var(--blue-surface)', border: '1px solid var(--blue-border)' }}>
        <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
          <strong style={{ color: 'var(--blue-text)' }}>Cryptographic Guarantee & Security Boundaries:</strong>{' '}
          Each event incorporates the SHA-256 hash of its predecessor to detect insertion, reordering, or tampering.
          As documented in our threat model, a hash chain alone cannot prevent a privileged database administrator from rewriting
          the entire database table; in enterprise production, independent external append-only log checkpoints or ledger anchors should be paired with this chain.
        </div>
      </div>

      {/* Audit Event Table */}
      <div className="glass-panel">
        <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '16px' }}>Global Event Ledger</h2>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            Retrieving audit ledger...
          </div>
        ) : auditEvents.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            No audit events recorded yet.
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
                  <th>Details</th>
                  <th>Correlation ID</th>
                  <th>Current Event Hash</th>
                </tr>
              </thead>
              <tbody>
                {auditEvents.map((evt) => (
                  <tr key={evt.id}>
                    <td style={{ color: 'var(--text-muted)', fontSize: '12px', whiteSpace: 'nowrap' }}>
                      {new Date(evt.timestamp).toLocaleString()}
                    </td>
                    <td style={{ fontSize: '13px' }}>{evt.actorEmail}</td>
                    <td style={{ fontWeight: 600 }}>{evt.action}</td>
                    <td>
                      <span className={`badge ${evt.outcome === 'SUCCESS' ? 'badge-read' : 'badge-danger'}`}>
                        {evt.outcome}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '13px', maxWidth: '300px' }}>
                      {evt.details}
                    </td>
                    <td style={{ fontSize: '12px' }}>
                      <span className="hash-code">{evt.correlationId ? evt.correlationId.slice(0, 8) + '...' : '-'}</span>
                    </td>
                    <td style={{ fontSize: '12px' }}>
                      <span className="hash-code" title={evt.currentEventHash}>
                        {evt.currentEventHash ? evt.currentEventHash.slice(0, 12) + '...' : '-'}
                      </span>
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
