import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import ConfirmModal from '../components/ConfirmModal';

export default function DocumentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [document, setDocument] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copiedHash, setCopiedHash] = useState(false);

  // Edit metadata modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [editName, setEditName] = useState('');
  const [editCategory, setEditCategory] = useState('');
  const [editDesc, setEditDesc] = useState('');
  const [savingEdit, setSavingEdit] = useState(false);

  // Delete modal state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    loadDocumentDetails();
  }, [id]);

  const loadDocumentDetails = async () => {
    try {
      setLoading(true);
      const [docData, auditData] = await Promise.all([
        api.getDocument(id),
        api.getDocumentAudit(id).catch(() => [])
      ]);
      setDocument(docData);
      setAuditLogs(auditData);
      setEditName(docData.originalDisplayName);
      setEditCategory(docData.category);
      setEditDesc(docData.description || '');
      setError('');
    } catch (err) {
      setError(err.message || 'Unable to retrieve document. You may not have permission.');
    } finally {
      setLoading(false);
    }
  };

  const copyChecksum = () => {
    if (document?.sha256Checksum) {
      navigator.clipboard.writeText(document.sha256Checksum);
      setCopiedHash(true);
      setTimeout(() => setCopiedHash(false), 2000);
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSavingEdit(true);
    try {
      const updated = await api.updateDocument(id, {
        originalDisplayName: editName,
        category: editCategory,
        description: editDesc,
      });
      setDocument(updated);
      setShowEditModal(false);
      loadDocumentDetails();
    } catch (err) {
      alert(err.message || 'Failed to update metadata');
    } finally {
      setSavingEdit(false);
    }
  };

  const handleDeleteConfirm = async () => {
    setDeleting(true);
    try {
      await api.deleteDocument(id);
      navigate('/documents', { replace: true });
    } catch (err) {
      alert(err.message || 'Failed to delete document');
      setShowDeleteModal(false);
    } finally {
      setDeleting(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>Loading document details...</div>;
  }

  if (error || !document) {
    return (
      <div style={{ maxWidth: '600px', margin: '40px auto' }}>
        <div className="glass-panel" style={{ textAlign: 'center', padding: '40px 24px' }}>
          <div style={{ fontSize: '36px', marginBottom: '14px' }}>🔒</div>
          <h2 style={{ fontSize: '20px', fontWeight: 600, marginBottom: '8px' }}>Access Denied or Not Found</h2>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '24px', fontSize: '14px' }}>
            {error || 'This document does not exist or your account has not been granted access to it.'}
          </p>
          <Link to="/documents" className="btn btn-primary">
            Back to Documents
          </Link>
        </div>
      </div>
    );
  }

  const isOwner = document.currentUserPermission === 'OWNER';
  const canEdit = isOwner || document.currentUserPermission === 'EDIT';

  return (
    <div>
      <div style={{ marginBottom: '20px' }}>
        <Link to="/documents" style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
          &larr; Back to all documents
        </Link>
      </div>

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '28px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '6px' }}>
            <h1 style={{ fontSize: '24px', fontWeight: 700 }}>{document.originalDisplayName}</h1>
            <span className={`badge badge-${document.currentUserPermission.toLowerCase()}`}>
              {document.currentUserPermission}
            </span>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Classification: <strong style={{ color: 'var(--text-primary)' }}>{document.category}</strong> • Owned by {document.ownerEmail}
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <a
            href={api.downloadDocumentUrl(document.id)}
            className="btn btn-primary"
            download
          >
            Download Document
          </a>

          {canEdit && (
            <button onClick={() => setShowEditModal(true)} className="btn btn-secondary">
              Edit Metadata
            </button>
          )}

          {isOwner && (
            <>
              <Link to={`/documents/${document.id}/sharing`} className="btn btn-secondary">
                Manage Access
              </Link>
              <button onClick={() => setShowDeleteModal(true)} className="btn btn-danger">
                Delete
              </button>
            </>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px', marginBottom: '32px' }}>
        {/* Document Specifications Panel */}
        <div className="glass-panel">
          <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '18px' }}>Security & Specifications</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '14px' }}>
            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '12px' }}>DOCUMENT ID</span>
              <span className="hash-code">{document.id}</span>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '12px' }}>MIME CONTENT TYPE</span>
              <code>{document.mimeType}</code>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '12px' }}>FILE SIZE</span>
              <span>{formatFileSize(document.fileSize)} ({document.fileSize.toLocaleString()} bytes)</span>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '12px' }}>UPLOAD TIMESTAMP</span>
              <span>{new Date(document.createdAt).toLocaleString()}</span>
            </div>
            <div>
              <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '12px' }}>DESCRIPTION</span>
              <span style={{ color: 'var(--text-secondary)' }}>
                {document.description || 'No description provided.'}
              </span>
            </div>
          </div>
        </div>

        {/* Cryptographic Integrity Panel */}
        <div className="glass-panel" style={{ borderTop: '4px solid var(--blue-primary)' }}>
          <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '14px' }}>Cryptographic Integrity Checksum</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '13px', marginBottom: '16px' }}>
            This immutable SHA-256 fingerprint was computed upon upload to guarantee file authenticity and detect unauthorized content modifications.
          </p>

          <div style={{ marginBottom: '16px' }}>
            <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '12px', marginBottom: '6px' }}>SHA-256 CHECKSUM</span>
            <div style={{ background: 'var(--blue-surface)', padding: '10px 12px', borderRadius: '8px', border: '1px solid var(--blue-border)' }}>
              <span className="hash-code" style={{ fontSize: '12px' }}>{document.sha256Checksum}</span>
            </div>
          </div>

          <button onClick={copyChecksum} className="btn btn-secondary btn-sm">
            {copiedHash ? '✓ Checksum Copied!' : 'Copy SHA-256 Checksum'}
          </button>
        </div>
      </div>

      {/* Audit History for this Document */}
      <div className="glass-panel">
        <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '16px' }}>
          Document Activity & Audit History
        </h2>

        {auditLogs.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>No audit records available for this document.</p>
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
                {auditLogs.map((log) => (
                  <tr key={log.id}>
                    <td style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
                      {new Date(log.timestamp).toLocaleString()}
                    </td>
                    <td style={{ fontSize: '13px' }}>{log.actorEmail}</td>
                    <td style={{ fontWeight: 600 }}>{log.action}</td>
                    <td>
                      <span className={`badge ${log.outcome === 'SUCCESS' ? 'badge-read' : 'badge-danger'}`}>
                        {log.outcome}
                      </span>
                    </td>
                    <td style={{ fontSize: '12px' }}>
                      <span className="hash-code">{log.correlationId ? log.correlationId.slice(0, 8) + '...' : '-'}</span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                      {log.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Edit Metadata Modal */}
      {showEditModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '16px' }}>Edit Document Metadata</h2>
            <form onSubmit={handleEditSubmit}>
              <div className="form-group">
                <label className="form-label">Document Display Name</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Category</label>
                <select
                  className="form-control"
                  value={editCategory}
                  onChange={(e) => setEditCategory(e.target.value)}
                >
                  <option value="General">General</option>
                  <option value="Legal">Legal</option>
                  <option value="Finance">Finance</option>
                  <option value="Security">Security</option>
                  <option value="Confidential">Confidential</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea
                  className="form-control"
                  rows="3"
                  value={editDesc}
                  onChange={(e) => setEditDesc(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button
                  type="button"
                  disabled={savingEdit}
                  onClick={() => setShowEditModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={savingEdit}
                  className="btn btn-primary"
                >
                  {savingEdit ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      <ConfirmModal
        isOpen={showDeleteModal}
        title="Delete Document"
        message={`Are you sure you want to delete "${document.originalDisplayName}"? Access for all granted users will be permanently revoked immediately.`}
        confirmText={deleting ? 'Deleting...' : 'Delete Document'}
        confirmVariant="danger"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setShowDeleteModal(false)}
      />
    </div>
  );
}
