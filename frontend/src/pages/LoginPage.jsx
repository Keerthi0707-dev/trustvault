import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSubmitting(true);
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      setErrorMsg(err.message || 'Invalid email or password');
    } finally {
      setSubmitting(false);
    }
  };

  const setDemoCredentials = (demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setErrorMsg('');
  };

  return (
    <div style={{ maxWidth: '440px', margin: '40px auto 0' }}>
      <div className="glass-panel" style={{ padding: '36px 30px' }}>
        <div style={{ textAlign: 'center', marginBottom: '28px' }}>
          <div
            className="brand-icon"
            style={{ width: '48px', height: '48px', fontSize: '20px', margin: '0 auto 14px' }}
          >
            TV
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '6px' }}>Sign in to TrustVault</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Military-grade secure document management & audit
          </p>
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
              placeholder="e.g. owner@trustvault.local"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              required
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '10px' }}
          >
            {submitting ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div style={{ marginTop: '30px', borderTop: '1px solid var(--border-subtle)', paddingTop: '20px' }}>
          <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '10px', textAlign: 'center', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Quick Demo Accounts
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setDemoCredentials('owner@trustvault.local', 'OwnerSecretPass123!')}
            >
              Document Owner
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setDemoCredentials('viewer@trustvault.local', 'ViewerSecretPass123!')}
            >
              Viewer (Recipient)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setDemoCredentials('admin@trustvault.local', 'AdminSecretPass123!')}
            >
              Security Admin
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setDemoCredentials('suspended@trustvault.local', 'SuspendedPass123!')}
            >
              Suspended User
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
