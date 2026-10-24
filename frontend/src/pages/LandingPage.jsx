import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LandingPage() {
  const { user, isAuthenticated } = useAuth();

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto' }}>
      {/* Hero Section */}
      <section style={{ textAlign: 'center', padding: '40px 10px 56px' }}>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '8px',
          background: 'rgba(56, 189, 248, 0.1)',
          border: '1px solid rgba(56, 189, 248, 0.25)',
          borderRadius: '9999px',
          padding: '6px 16px',
          fontSize: '13px',
          fontWeight: 600,
          color: 'var(--accent-cyan)',
          marginBottom: '24px',
          letterSpacing: '0.5px'
        }}>
          <span style={{
            display: 'inline-block',
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            background: 'var(--accent-teal)',
            boxShadow: '0 0 8px var(--accent-teal)'
          }} />
          MILITARY-GRADE ZERO-TRUST DOCUMENT VAULT
        </div>

        <h1 style={{
          fontSize: 'clamp(32px, 5vw, 52px)',
          fontWeight: 800,
          lineHeight: 1.15,
          letterSpacing: '-1px',
          marginBottom: '20px',
          background: 'linear-gradient(180deg, #ffffff 0%, #94a3b8 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent'
        }}>
          Where the wrong person can <span style={{
            background: 'linear-gradient(135deg, #38bdf8 0%, #818cf8 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>never read the file</span>,<br />
          and you can prove who did.
        </h1>

        <p style={{
          fontSize: 'clamp(16px, 2vw, 19px)',
          color: 'var(--text-secondary)',
          maxWidth: '740px',
          margin: '0 auto 36px',
          lineHeight: 1.6
        }}>
          Every company shares confidential salary letters, contracts, and audit files.
          TrustVault solves the two costliest problems: <strong>broken permissions</strong> and <strong>unprovable audit trails</strong>,
          using zero-trust access control, immediate revocation, and a tamper-evident SHA-256 cryptographic chain.
        </p>

        {/* Dynamic CTA Group */}
        {isAuthenticated ? (
          <div style={{ display: 'flex', gap: '14px', justifyContent: 'center', flexWrap: 'wrap' }}>
            <Link to="/dashboard" className="btn btn-primary" style={{ padding: '12px 28px', fontSize: '15px' }}>
              <span>Open Dashboard</span>
              <span>&rarr;</span>
            </Link>
            <Link to="/documents" className="btn btn-secondary" style={{ padding: '12px 24px', fontSize: '15px' }}>
              <span>View Documents</span>
            </Link>
          </div>
        ) : (
          <div>
            <div style={{
              display: 'flex',
              gap: '16px',
              justifyContent: 'center',
              flexWrap: 'wrap',
              marginBottom: '16px'
            }}>
              <Link
                to="/register"
                className="btn btn-primary"
                style={{
                  padding: '14px 32px',
                  fontSize: '16px',
                  fontWeight: 600,
                  boxShadow: '0 4px 20px rgba(2, 132, 199, 0.4)'
                }}
              >
                <span>✨ Create Free Account</span>
                <span style={{ fontSize: '18px' }}>&rarr;</span>
              </Link>
              <Link
                to="/login"
                className="btn btn-secondary"
                style={{
                  padding: '14px 28px',
                  fontSize: '16px',
                  fontWeight: 500
                }}
              >
                <span>Sign In to Vault</span>
              </Link>
            </div>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              New users get instant <strong>Document Owner</strong> privileges to upload, share, and audit confidential files.
            </p>
          </div>
        )}
      </section>

      {/* Problem & Solution Comparison */}
      <section style={{ marginBottom: '56px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>
            The Two Expensive Failures TrustVault Solves
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>
            Built specifically to defeat unauthorized document reach and answer every compliance inquiry with mathematical certainty.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          <div className="glass-panel" style={{ borderLeft: '4px solid var(--accent-rose)' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: '#fda4af', textTransform: 'uppercase', marginBottom: '8px' }}>
              The Permissions Crisis
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '10px' }}>
              Accidental Over-Sharing & Lingering Links
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '16px' }}>
              Someone is granted access to a shared folder to read one contract and inherits 40 salary files.
              Employees leave or contracts expire, but direct links still resolve months later.
            </p>
            <div style={{
              background: 'rgba(244, 63, 94, 0.1)',
              border: '1px solid rgba(244, 63, 94, 0.25)',
              borderRadius: 'var(--radius-sm)',
              padding: '10px 14px',
              fontSize: '13px',
              color: '#fda4af'
            }}>
              <strong>TrustVault Solution:</strong> Strict Anti-IDOR (404 Not Found), individual recipient grants (READ/EDIT), time-bound expirations, and real-time one-click revocation.
            </div>
          </div>

          <div className="glass-panel" style={{ borderLeft: '4px solid var(--accent-cyan)' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--accent-cyan)', textTransform: 'uppercase', marginBottom: '8px' }}>
              The Audit Void
            </div>
            <h3 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '10px' }}>
              "Who opened this, and when?"
            </h3>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '16px' }}>
              When a document leaks to a competitor or the press, traditional storage logs are either missing,
              mutable, or unable to prove whether an admin or attacker altered timestamps after the incident.
            </p>
            <div style={{
              background: 'rgba(56, 189, 248, 0.1)',
              border: '1px solid rgba(56, 189, 248, 0.25)',
              borderRadius: 'var(--radius-sm)',
              padding: '10px 14px',
              fontSize: '13px',
              color: '#7dd3fc'
            }}>
              <strong>TrustVault Solution:</strong> Append-only SHA-256 cryptographic chain. Every upload, access attempt, download, and revocation is hashed into an immutable ledger.
            </div>
          </div>
        </div>
      </section>

      {/* Core Platform Architecture Features */}
      <section style={{ marginBottom: '64px' }}>
        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <h2 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '8px' }}>
            Built With Zero-Trust at Every Layer
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>
            From cryptographic storage to real-time revocation and AI-assisted governance.
          </p>
        </div>

        <div className="grid-cards">
          <div className="glass-panel">
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(56, 189, 248, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              🔒
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '8px' }}>
              Zero-Trust & Anti-IDOR Defense
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Unauthorized file requests return strict <code>404 Not Found</code> rather than <code>403 Forbidden</code>,
              eliminating metadata leakage and ID enumeration attacks.
            </p>
          </div>

          <div className="glass-panel">
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(16, 185, 129, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              🔗
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '8px' }}>
              SHA-256 Cryptographic Chain
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Each audit log entry calculates <code>currentHash = SHA256(prevHash + action + timestamp + payload)</code>.
              Database tampering is immediately detected by the automated verification engine.
            </p>
          </div>

          <div className="glass-panel">
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(244, 63, 94, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              ⚡
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '8px' }}>
              Immediate Real-Time Revocation
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Revoke recipient access with a single click. Document downloads and previews are terminated
              instantly without relying on stale tokens or caching windows.
            </p>
          </div>

          <div className="glass-panel">
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(99, 102, 241, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              🤖
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '8px' }}>
              AI Least-Privilege Advisor
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Automated sensitivity heuristics analyze file titles, categories, and sharing parameters to warn
              against over-privileged grants and recommend minimal expiration policies.
            </p>
          </div>

          <div className="glass-panel">
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(245, 158, 11, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              📦
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '8px' }}>
              Private Server-Mediated Storage
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Raw files are stored in private cloud storage and streamed exclusively through authenticated,
              audited Spring Boot proxy streams. Direct storage URLs are never exposed to clients.
            </p>
          </div>

          <div className="glass-panel">
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              background: 'rgba(168, 85, 247, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              👥
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '8px' }}>
              Role-Based Governance
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Dedicated Owner, Viewer, and Admin personas. Suspended users are instantly barred at the security
              filter layer and flagged in audit logs.
            </p>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section style={{ marginBottom: '64px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <h2 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>
            How Document Protection Works
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>
            Three seamless steps from initial upload to compliance verification.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px' }}>
          <div className="glass-panel" style={{ position: 'relative' }}>
            <div style={{
              position: 'absolute',
              top: '16px',
              right: '20px',
              fontSize: '36px',
              fontWeight: 900,
              color: 'rgba(255, 255, 255, 0.05)'
            }}>
              01
            </div>
            <h4 style={{ fontSize: '17px', fontWeight: 600, marginBottom: '10px', color: 'var(--accent-cyan)' }}>
              1. Upload & Hash
            </h4>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
              Upload contracts, compensation files, or code. TrustVault streams the file to private storage,
              computes a SHA-256 payload checksum, and records the initial tamper-evident audit event.
            </p>
          </div>

          <div className="glass-panel" style={{ position: 'relative' }}>
            <div style={{
              position: 'absolute',
              top: '16px',
              right: '20px',
              fontSize: '36px',
              fontWeight: 900,
              color: 'rgba(255, 255, 255, 0.05)'
            }}>
              02
            </div>
            <h4 style={{ fontSize: '17px', fontWeight: 600, marginBottom: '10px', color: 'var(--accent-teal)' }}>
              2. Share With Least Privilege
            </h4>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
              Grant specific recipients READ or EDIT access with optional expiration dates.
              The AI Access Advisor evaluates sensitivity and alerts you to over-broad scopes.
            </p>
          </div>

          <div className="glass-panel" style={{ position: 'relative' }}>
            <div style={{
              position: 'absolute',
              top: '16px',
              right: '20px',
              fontSize: '36px',
              fontWeight: 900,
              color: 'rgba(255, 255, 255, 0.05)'
            }}>
              03
            </div>
            <h4 style={{ fontSize: '17px', fontWeight: 600, marginBottom: '10px', color: 'var(--accent-indigo)' }}>
              3. Prove Access & Audit
            </h4>
            <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
              Every view, download, update, and revocation is cryptographically chained.
              Security administrators can verify chain mathematical integrity at any time.
            </p>
          </div>
        </div>
      </section>

      {/* Bottom CTA Banner */}
      <section className="glass-panel" style={{
        textAlign: 'center',
        padding: '48px 24px',
        background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.9) 0%, rgba(30, 41, 59, 0.6) 100%)',
        border: '1px solid rgba(56, 189, 248, 0.25)',
        marginBottom: '40px'
      }}>
        <h3 style={{ fontSize: '26px', fontWeight: 700, marginBottom: '12px' }}>
          Ready to Secure Your Confidential Documents?
        </h3>
        <p style={{ color: 'var(--text-secondary)', maxWidth: '580px', margin: '0 auto 24px', fontSize: '15px' }}>
          Create an account in seconds to upload your first document, or sign in to test the end-to-end audit trail.
        </p>
        <div style={{ display: 'flex', gap: '14px', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link to="/register" className="btn btn-primary" style={{ padding: '12px 28px', fontSize: '15px' }}>
            Sign Up as Owner
          </Link>
          <Link to="/login" className="btn btn-secondary" style={{ padding: '12px 24px', fontSize: '15px' }}>
            Sign In Existing Account
          </Link>
        </div>
      </section>
    </div>
  );
}
