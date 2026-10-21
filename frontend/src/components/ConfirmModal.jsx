import React from 'react';

export default function ConfirmModal({ isOpen, title, message, confirmText = 'Confirm', confirmVariant = 'danger', onConfirm, onCancel }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h3 style={{ marginBottom: '12px', fontSize: '18px', fontWeight: 600 }}>{title}</h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '24px', fontSize: '14px' }}>{message}</p>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
          <button type="button" onClick={onCancel} className="btn btn-secondary">
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className={`btn btn-${confirmVariant}`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
