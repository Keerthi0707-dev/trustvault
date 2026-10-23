import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api/api';
import { useAuth } from '../context/AuthContext';

export default function DocumentsPage() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Search & Filter state
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('category') || '');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  // Upload modal state
  const [showUploadModal, setShowUploadModal] = useState(searchParams.get('action') === 'upload');
  const [uploadFile, setUploadFile] = useState(null);
  const [uploadCategory, setUploadCategory] = useState('General');
  const [uploadDescription, setUploadDescription] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');

  useEffect(() => {
    fetchDocuments();
  }, [searchQuery, selectedCategory, startDate, endDate]);

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const startIso = startDate ? new Date(startDate + 'T00:00:00.000Z').toISOString() : '';
      const endIso = endDate ? new Date(endDate + 'T23:59:59.999Z').toISOString() : '';
      const data = await api.listDocuments(searchQuery, selectedCategory, startIso, endIso);
      setDocuments(data);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchDocuments();
  };

  const handleClearFilters = () => {
    setSearchQuery('');
    setSelectedCategory('');
    setStartDate('');
    setEndDate('');
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!uploadFile) {
      setUploadError('Please select a file to upload');
      return;
    }

    setUploadError('');
    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', uploadFile);
      formData.append('category', uploadCategory);
      formData.append('description', uploadDescription);

      await api.uploadDocument(formData);
      setSuccessMsg(`Document "${uploadFile.name}" uploaded successfully and secured.`);
      setShowUploadModal(false);
      setUploadFile(null);
      setUploadDescription('');
      fetchDocuments();
    } catch (err) {
      setUploadError(err.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '4px' }}>Document Vault</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Browse and manage confidential documents authorized for your account
          </p>
        </div>
        {(user?.role === 'OWNER' || user?.role === 'ADMIN') && (
          <button onClick={() => setShowUploadModal(true)} className="btn btn-primary">
            + Upload Document
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMsg && <div className="alert alert-success">{successMsg}</div>}

      {/* Filter and Search Bar */}
      <div className="glass-panel" style={{ padding: '16px 20px', marginBottom: '24px' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ flex: 2, minWidth: '220px' }}>
            <input
              type="text"
              className="form-control"
              placeholder="Search by filename, owner email, or description..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <div style={{ width: '160px' }}>
            <select
              className="form-control"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <option value="">All Categories</option>
              <option value="General">General</option>
              <option value="Legal">Legal</option>
              <option value="Finance">Finance</option>
              <option value="Security">Security</option>
              <option value="Confidential">Confidential</option>
            </select>
          </div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            <label style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>From:</label>
            <input
              type="date"
              className="form-control"
              style={{ width: '135px', padding: '6px 10px', fontSize: '13px' }}
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
          </div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            <label style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>To:</label>
            <input
              type="date"
              className="form-control"
              style={{ width: '135px', padding: '6px 10px', fontSize: '13px' }}
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-secondary">
            Filter
          </button>
          {(searchQuery || selectedCategory || startDate || endDate) && (
            <button type="button" onClick={handleClearFilters} className="btn btn-sm" style={{ background: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
              Clear
            </button>
          )}
        </form>
      </div>

      {/* Document Table */}
      <div className="glass-panel">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-secondary)' }}>
            Searching vault records...
          </div>
        ) : documents.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px 20px', color: 'var(--text-secondary)' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-primary)' }}>
              No accessible documents found
            </h3>
            <p style={{ fontSize: '14px', maxWidth: '420px', margin: '0 auto 16px' }}>
              {searchQuery || selectedCategory
                ? 'Try adjusting your search criteria or category filter.'
                : 'Upload a document or request access from a document owner.'}
            </p>
            {(user?.role === 'OWNER' || user?.role === 'ADMIN') && (
              <button onClick={() => setShowUploadModal(true)} className="btn btn-primary btn-sm">
                Upload Document Now
              </button>
            )}
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Document Name</th>
                  <th>Category</th>
                  <th>Size</th>
                  <th>Owner</th>
                  <th>Permission</th>
                  <th>Uploaded</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {documents.map((doc) => (
                  <tr key={doc.id}>
                    <td style={{ fontWeight: 500 }}>
                      <Link to={`/documents/${doc.id}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                        <span>📄</span>
                        <span>{doc.originalDisplayName}</span>
                      </Link>
                    </td>
                    <td>
                      <span style={{ color: 'var(--text-secondary)' }}>{doc.category}</span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                      {formatFileSize(doc.fileSize)}
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
                      <div style={{ display: 'inline-flex', gap: '6px' }}>
                        <a
                          href={api.downloadDocumentUrl(doc.id)}
                          className="btn btn-secondary btn-sm"
                          download
                        >
                          Download
                        </a>
                        {doc.currentUserPermission === 'OWNER' && (
                          <Link to={`/documents/${doc.id}/sharing`} className="btn btn-secondary btn-sm">
                            Share
                          </Link>
                        )}
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

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '14px' }}>
              Upload Confidential Document
            </h2>

            {uploadError && <div className="alert alert-error">{uploadError}</div>}

            <form onSubmit={handleUploadSubmit}>
              <div className="form-group">
                <label className="form-label">Select File *</label>
                <input
                  type="file"
                  required
                  className="form-control"
                  onChange={(e) => setUploadFile(e.target.files[0])}
                />
                <span style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px', display: 'block' }}>
                  Supported formats: PDF, DOCX, XLSX, PPTX, TXT, CSV, MD, PNG, JPG (Max 25MB). Executable scripts and HTML are strictly prohibited.
                </span>
              </div>

              <div className="form-group">
                <label className="form-label">Category</label>
                <select
                  className="form-control"
                  value={uploadCategory}
                  onChange={(e) => setUploadCategory(e.target.value)}
                >
                  <option value="General">General</option>
                  <option value="Legal">Legal</option>
                  <option value="Finance">Finance</option>
                  <option value="Security">Security</option>
                  <option value="Confidential">Confidential</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Description (Optional)</label>
                <textarea
                  className="form-control"
                  rows="3"
                  value={uploadDescription}
                  onChange={(e) => setUploadDescription(e.target.value)}
                  placeholder="Add notes about document contents or classification level..."
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button
                  type="button"
                  disabled={uploading}
                  onClick={() => setShowUploadModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={uploading}
                  className="btn btn-primary"
                >
                  {uploading ? 'Validating & Uploading...' : 'Upload & Encrypt'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
