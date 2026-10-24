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
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setErrorMsg(err.message || 'Registration failed. Please check your details.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: '980px', margin: '20px auto 40px' }}>
      <div className="auth-vault-page">
        {/* Left Column: Security Provisioning Showcase */}
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
                  SECURITY CLEARANCE
                </div>
                <div style={{
                  fontSize: '17px',
                  fontFamily: 'var(--font-display)',
                  fontWeight: 700,
                  color: '#ffffff',
                  letterSpacing: '-0.3px'
                }}>
                  Owner Onboarding
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
              Instant Document Owner Privileges
            </h2>
            <p style={{
              color: '#94a3b8',
              fontSize: '13.5px',
              lineHeight: 1.6,
              marginBottom: '24px',
              fontFamily: 'var(--font-body)'
            }}>
              Every newly registered account is instantly provisioned as an enterprise <strong>Document Owner</strong> with end-to-end audit accountability.
            </p>

            {/* Clearance Level Box */}
            <div style={{
              background: 'rgba(37, 99, 235, 0.12)',
              border: '1px solid rgba(59, 130, 246, 0.35)',
              borderRadius: 'var(--radius-md)',
              padding: '14px 16px',
              marginBottom: '24px'
            }}>
              <div style={{
                fontFamily: 'var(--font-mono)',
                fontSize: '11px',
                color: '#93c5fd',
                fontWeight: 700,
                letterSpacing: '0.8px',
                marginBottom: '6px'
              }}>
                LEVEL 4 CLEARANCE INCLUDED
              </div>
              <p style={{ fontSize: '12.5px', color: '#cbd5e1', lineHeight: 1.5, margin: 0, fontFamily: 'var(--font-body)' }}>
                Upload confidential documents, configure recipient scopes (READ / EDIT), set granular expiration deadlines, and trigger real-time revocations.
              </p>
            </div>

            {/* 4 Pillars of TrustVault */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '13px', color: '#94a3b8' }}>
                <span style={{ color: '#60a5fa' }}>✔</span> Zero-Trust Anti-IDOR (Strict 404 on access denials)
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '13px', color: '#94a3b8' }}>
                <span style={{ color: '#34d399' }}>✔</span> SHA-256 Tamper-Evident Immutable Audit Log
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '13px', color: '#94a3b8' }}>
                <span style={{ color: '#fbbf24' }}>✔</span> AI Least-Privilege Advisor Sensitivity Warning
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '13px', color: '#94a3b8' }}>
                <span style={{ color: '#f87171' }}>✔</span> One-Click Sub-Second Real-Time Revocation
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
            ENTERPRISE ZERO-TRUST READY
          </div>
        </div>

        {/* Right Column: Registration Terminal */}
        <div className="auth-vault-terminal">
          <div style={{ marginBottom: '22px' }}>
            <h1 style={{
              fontSize: '22px',
              fontFamily: 'var(--font-display)',
              fontWeight: 800,
              color: '#ffffff',
              marginBottom: '6px'
            }}>
              Create Owner Account
            </h1>
            <p style={{ color: '#94a3b8', fontSize: '13.5px', fontFamily: 'var(--font-body)' }}>
              Set up your personal vault to begin sharing securely.
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
              <label className="form-label">Work or Personal Email</label>
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
              <label className="form-label">Password (Min 8 Characters)</label>
              <input
                type="password"
                required
                minLength={8}
                className="form-control"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••••••"
                autoComplete="new-password"
              />
              <span style={{ fontSize: '11px', color: '#64748b', display: 'block', marginTop: '4px', fontFamily: 'var(--font-mono)' }}>
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
              style={{
                width: '100%',
                marginTop: '10px',
                padding: '12px',
                fontSize: '14.5px',
                fontWeight: 600,
                background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
                boxShadow: '0 4px 14px rgba(37, 99, 235, 0.4)'
              }}
            >
              {submitting ? 'Provisioning Vault Clearance...' : 'Create Owner Account'}
            </button>
          </form>

          <div style={{ marginTop: '22px', borderTop: '1px solid rgba(255, 255, 255, 0.08)', paddingTop: '16px', textAlign: 'center' }}>
            <p style={{ fontSize: '13.5px', color: '#94a3b8', fontFamily: 'var(--font-body)' }}>
              Already registered?{' '}
              <Link to="/login" style={{ fontWeight: 600, color: '#60a5fa' }}>
                Sign In to Vault &rarr;
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
