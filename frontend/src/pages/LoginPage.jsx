import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const location = useLocation();
  const [email, setEmail] = useState(location.state?.demoEmail || '');
  const [password, setPassword] = useState(location.state?.demoPassword || '');
  const [errorMsg, setErrorMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const from = location.state?.from?.pathname || '/dashboard';

  useEffect(() => {
    if (location.state?.demoEmail && location.state?.demoPassword) {
      setEmail(location.state.demoEmail);
      setPassword(location.state.demoPassword);
    }
  }, [location.state]);

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
    <div style={{ maxWidth: '980px', margin: '20px auto 40px' }}>
      <div className="auth-vault-page">
        {/* Left Column: Security Clearance Showcase */}
        <div className="auth-vault-showcase">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
              <div
                className="brand-icon"
                style={{
                  width: '42px',
                  height: '42px',
                  fontSize: '18px',
                  boxShadow: '0 0 16px rgba(37, 99, 235, 0.45)'
                }}
              >
                TV
              </div>
              <div>
                <div style={{
                  fontSize: '11px',
                  fontFamily: 'var(--font-mono)',
                  color: '#60a5fa',
                  letterSpacing: '1px',
                  fontWeight: 700
                }}>
                  CLEARANCE PORTAL
                </div>
                <div style={{
                  fontSize: '17px',
                  fontFamily: 'var(--font-display)',
                  fontWeight: 700,
                  color: '#ffffff',
                  letterSpacing: '-0.3px'
                }}>
                  TrustVault Security
                </div>
              </div>
            </div>

            <h2 style={{
              fontSize: '24px',
              fontFamily: 'var(--font-display)',
              fontWeight: 800,
              color: '#ffffff',
              lineHeight: 1.25,
              marginBottom: '12px',
              letterSpacing: '-0.5px'
            }}>
              Zero-Trust Document Access & Proof
            </h2>
            <p style={{
              color: '#94a3b8',
              fontSize: '13.5px',
              lineHeight: 1.6,
              marginBottom: '28px',
              fontFamily: 'var(--font-body)'
            }}>
              Every document is protected with zero-trust permissions, and every interaction is sealed into a tamper-evident SHA-256 chain.
            </p>

            {/* 4-Color Mature Security Guarantees */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div className="auth-security-item">
                <div className="auth-security-icon" style={{ background: 'rgba(37, 99, 235, 0.2)', border: '1px solid rgba(59, 130, 246, 0.4)' }}>
                  🔒
                </div>
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: '#93c5fd', fontFamily: 'var(--font-heading)' }}>
                    Anti-IDOR Defense Layer
                  </div>
                  <div style={{ fontSize: '12px', color: '#64748b', lineHeight: 1.4, fontFamily: 'var(--font-body)' }}>
                    Unauthorized document inquiries return 404 Not Found to prevent enumeration.
                  </div>
                </div>
              </div>

              <div className="auth-security-item">
                <div className="auth-security-icon" style={{ background: 'rgba(5, 150, 105, 0.2)', border: '1px solid rgba(16, 185, 129, 0.4)' }}>
                  🔗
                </div>
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: '#6ee7b7', fontFamily: 'var(--font-heading)' }}>
                    SHA-256 Cryptographic Chain
                  </div>
                  <div style={{ fontSize: '12px', color: '#64748b', lineHeight: 1.4, fontFamily: 'var(--font-body)' }}>
                    Audit events are linked immutably to detect retrospective database modifications.
                  </div>
                </div>
              </div>

              <div className="auth-security-item">
                <div className="auth-security-icon" style={{ background: 'rgba(217, 119, 6, 0.2)', border: '1px solid rgba(245, 158, 11, 0.4)' }}>
                  🤖
                </div>
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: '#fcd34d', fontFamily: 'var(--font-heading)' }}>
                    AI Least-Privilege Advisor
                  </div>
                  <div style={{ fontSize: '12px', color: '#64748b', lineHeight: 1.4, fontFamily: 'var(--font-body)' }}>
                    Real-time sensitivity heuristics prevent over-broad recipient scoping.
                  </div>
                </div>
              </div>

              <div className="auth-security-item">
                <div className="auth-security-icon" style={{ background: 'rgba(225, 29, 72, 0.2)', border: '1px solid rgba(244, 63, 94, 0.4)' }}>
                  ⚡
                </div>
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 700, color: '#fda4af', fontFamily: 'var(--font-heading)' }}>
                    Instant Real-Time Revocation
                  </div>
                  <div style={{ fontSize: '12px', color: '#64748b', lineHeight: 1.4, fontFamily: 'var(--font-body)' }}>
                    Revoked recipient tokens terminate immediately without caching delays.
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div style={{
            marginTop: '28px',
            paddingTop: '16px',
            borderTop: '1px solid rgba(255, 255, 255, 0.08)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            fontSize: '11px',
            color: '#64748b',
            fontFamily: 'var(--font-mono)'
          }}>
            <span style={{
              display: 'inline-block',
              width: '7px',
              height: '7px',
              borderRadius: '50%',
              background: '#10b981',
              boxShadow: '0 0 6px #10b981'
            }} />
            MILITARY GRADE 256-BIT ENCRYPTION • ACTIVE
          </div>
        </div>

        {/* Right Column: Authentication Terminal */}
        <div className="auth-vault-terminal">
          <div style={{ marginBottom: '22px' }}>
            <h1 style={{
              fontSize: '22px',
              fontFamily: 'var(--font-display)',
              fontWeight: 800,
              color: '#ffffff',
              marginBottom: '6px'
            }}>
              Sign In to Terminal
            </h1>
            <p style={{ color: '#94a3b8', fontSize: '13.5px', fontFamily: 'var(--font-body)' }}>
              Enter authorized credentials to access document vaults.
            </p>
          </div>

          {errorMsg && (
            <div style={{
              background: 'rgba(225, 29, 72, 0.15)',
              border: '1px solid rgba(244, 63, 94, 0.35)',
              borderRadius: 'var(--radius-md)',
              padding: '10px 14px',
              color: '#fecdd3',
              fontSize: '13px',
              marginBottom: '18px',
              fontFamily: 'var(--font-body)'
            }}>
              {errorMsg}
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
                autoComplete="email"
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
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="btn btn-primary"
              style={{
                width: '100%',
                marginTop: '8px',
                padding: '12px',
                fontSize: '14.5px',
                fontWeight: 600,
                background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
                boxShadow: '0 4px 14px rgba(37, 99, 235, 0.4)'
              }}
            >
              {submitting ? 'Authenticating Clearance...' : 'Sign In to Vault'}
            </button>
          </form>

          <div style={{ marginTop: '18px', textAlign: 'center' }}>
            <p style={{ fontSize: '13.5px', color: '#94a3b8', fontFamily: 'var(--font-body)' }}>
              New to TrustVault?{' '}
              <Link to="/register" style={{ fontWeight: 600, color: '#60a5fa' }}>
                Create Owner Account &rarr;
              </Link>
            </p>
          </div>

          {/* Quick Demo Clearance Keys */}
          <div style={{ marginTop: '24px', borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '16px' }}>
            <p style={{
              fontSize: '10.5px',
              color: '#64748b',
              marginBottom: '10px',
              textTransform: 'uppercase',
              letterSpacing: '0.8px',
              fontWeight: 700,
              fontFamily: 'var(--font-mono)'
            }}>
              Quick Evaluator Demo Clearance Keys
            </p>
            <div className="auth-demo-grid">
              <button
                type="button"
                className="auth-demo-key"
                onClick={() => setDemoCredentials('owner@trustvault.local', 'OwnerSecretPass123!')}
              >
                <span className="auth-demo-role-tag" style={{ background: 'rgba(37, 99, 235, 0.25)', color: '#93c5fd', border: '1px solid rgba(59, 130, 246, 0.4)' }}>
                  OWNER
                </span>
                <span className="auth-demo-name">Document Owner</span>
              </button>

              <button
                type="button"
                className="auth-demo-key"
                onClick={() => setDemoCredentials('viewer@trustvault.local', 'ViewerSecretPass123!')}
              >
                <span className="auth-demo-role-tag" style={{ background: 'rgba(5, 150, 105, 0.25)', color: '#6ee7b7', border: '1px solid rgba(16, 185, 129, 0.4)' }}>
                  VIEWER
                </span>
                <span className="auth-demo-name">Recipient (Viewer)</span>
              </button>

              <button
                type="button"
                className="auth-demo-key"
                onClick={() => setDemoCredentials('admin@trustvault.local', 'AdminSecretPass123!')}
              >
                <span className="auth-demo-role-tag" style={{ background: 'rgba(217, 119, 6, 0.25)', color: '#fcd34d', border: '1px solid rgba(245, 158, 11, 0.4)' }}>
                  AUDITOR
                </span>
                <span className="auth-demo-name">Security Admin</span>
              </button>

              <button
                type="button"
                className="auth-demo-key"
                onClick={() => setDemoCredentials('suspended@trustvault.local', 'SuspendedPass123!')}
              >
                <span className="auth-demo-role-tag" style={{ background: 'rgba(225, 29, 72, 0.25)', color: '#fda4af', border: '1px solid rgba(244, 63, 94, 0.4)' }}>
                  LOCKED
                </span>
                <span className="auth-demo-name">Suspended User</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
