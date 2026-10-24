import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (password.length < 8) {
      setErrorMsg('Password must be at least 8 characters long');
      return;
    }

    if (password !== confirmPassword) {
      setErrorMsg('Passwords do not match');
      return;
    }

    setSubmitting(true);
    try {
      await register(email.trim(), password);
      // Seamlessly redirects to dashboard as registered user
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setErrorMsg(err.message || 'Registration failed. Please check your details.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: '460px', margin: '40px auto 0' }}>
      <div className="glass-panel" style={{ padding: '36px 30px' }}>
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div
            className="brand-icon"
            style={{ width: '48px', height: '48px', fontSize: '20px', margin: '0 auto 14px' }}
          >
            TV
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '6px' }}>
            Create Your TrustVault Account
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Get started with zero-trust document security
          </p>
        </div>

        {/* Privilege Information Badge */}
        <div style={{
          background: 'var(--blue-surface)',
          border: '1px solid var(--blue-border)',
          borderRadius: 'var(--radius-md)',
          padding: '12px 14px',
          marginBottom: '20px',
          fontSize: '13px',
          lineHeight: 1.5,
          color: 'var(--blue-text)'
        }}>
          <strong style={{ color: 'var(--blue-text)' }}>🛡️ Automatic Owner Privileges:</strong> New accounts are provisioned with <strong>Document Owner</strong> permissions, allowing you to immediately upload, encrypt, share, and revoke access to your documents.
        </div>

        {errorMsg && (
          <div className="alert alert-error">
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input
              type="email"
              required
              className="form-control"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. yourname@company.com"
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              required
              minLength={8}
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="At least 8 characters"
              autoComplete="new-password"
            />
            <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block', marginTop: '4px' }}>
              Minimum 8 characters with letters, numbers, or symbols.
            </span>
          </div>

          <div className="form-group">
            <label className="form-label">Confirm Password</label>
            <input
              type="password"
              required
              minLength={8}
              className="form-control"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter password"
              autoComplete="new-password"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '10px', padding: '12px' }}
          >
            {submitting ? 'Creating account & securing session...' : 'Create Owner Account'}
          </button>
        </form>

        <div style={{ marginTop: '24px', borderTop: '1px solid var(--border-subtle)', paddingTop: '18px', textAlign: 'center' }}>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ fontWeight: 600, color: 'var(--accent-cyan)' }}>
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
