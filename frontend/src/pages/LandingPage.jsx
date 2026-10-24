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
          background: 'var(--blue-surface)',
          border: '1px solid var(--blue-border)',
          borderRadius: '9999px',
          padding: '6px 16px',
          fontSize: '12px',
          fontWeight: 700,
          color: 'var(--blue-text)',
          marginBottom: '24px',
          letterSpacing: '0.8px',
          fontFamily: 'var(--font-mono)'
        }}>
          <span style={{
            display: 'inline-block',
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            background: 'var(--green-primary)',
            boxShadow: '0 0 8px rgba(5, 150, 105, 0.5)'
          }} />
          MILITARY-GRADE ZERO-TRUST DOCUMENT VAULT
        </div>

        <h1 style={{
          fontSize: 'clamp(32px, 5vw, 54px)',
          fontFamily: 'var(--font-display)',
          fontWeight: 800,
          lineHeight: 1.15,
          letterSpacing: '-1.5px',
          marginBottom: '20px',
          color: 'var(--text-primary)'
        }}>
          Where the wrong person can{' '}
          <span style={{
            background: 'linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>
            never read the file
          </span>,<br />
          and you can prove who did.
        </h1>

        <p style={{
          fontSize: 'clamp(16px, 2vw, 19px)',
          fontFamily: 'var(--font-body)',
          color: 'var(--text-secondary)',
          maxWidth: '740px',
          margin: '0 auto 36px',
          lineHeight: 1.65
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
                  fontSize: '15.5px',
                  fontWeight: 600,
                  boxShadow: '0 4px 14px rgba(37, 99, 235, 0.3)'
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
                  fontSize: '15.5px',
                  fontWeight: 600
                }}
              >
                <span>Sign In to Vault</span>
              </Link>
            </div>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontFamily: 'var(--font-body)' }}>
              New users get instant <strong>Document Owner</strong> privileges to upload, share, and audit confidential files.
            </p>
          </div>
        )}
      </section>

      {/* Problem & Solution Comparison */}
      <section style={{ marginBottom: '56px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <h2 style={{ fontSize: '26px', fontFamily: 'var(--font-display)', fontWeight: 800, marginBottom: '8px', letterSpacing: '-0.5px' }}>
            The Two Expensive Failures TrustVault Solves
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px', fontFamily: 'var(--font-body)' }}>
            Built specifically to defeat unauthorized document reach and answer every compliance inquiry with mathematical certainty.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          {/* Card 1: Red accent (Permissions Crisis) */}
          <div className="glass-panel" style={{ borderLeft: '4px solid var(--rose-primary)' }}>
            <div style={{ fontSize: '11.5px', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--rose-text)', textTransform: 'uppercase', marginBottom: '8px', letterSpacing: '0.8px' }}>
              The Permissions Crisis
            </div>
            <h3 style={{ fontSize: '18px', fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '10px' }}>
              Accidental Over-Sharing & Lingering Links
            </h3>
            <p style={{ fontSize: '14px', fontFamily: 'var(--font-body)', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '16px' }}>
              Someone is granted access to a shared folder to read one contract and inherits 40 salary files.
              Employees leave or contracts expire, but direct links still resolve months later.
            </p>
            <div style={{
              background: 'var(--rose-surface)',
              border: '1px solid var(--rose-border)',
              borderRadius: 'var(--radius-sm)',
              padding: '10px 14px',
              fontSize: '13px',
              fontFamily: 'var(--font-body)',
              color: 'var(--rose-text)'
            }}>
              <strong>TrustVault Solution:</strong> Strict Anti-IDOR (404 Not Found), individual recipient grants (READ/EDIT), time-bound expirations, and real-time one-click revocation.
            </div>
          </div>

          {/* Card 2: Blue accent (Audit Void) */}
          <div className="glass-panel" style={{ borderLeft: '4px solid var(--blue-primary)' }}>
            <div style={{ fontSize: '11.5px', fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--blue-text)', textTransform: 'uppercase', marginBottom: '8px', letterSpacing: '0.8px' }}>
              The Audit Void
            </div>
            <h3 style={{ fontSize: '21px', fontFamily: 'var(--font-quote)', fontStyle: 'italic', fontWeight: 600, color: '#1e3a8a', marginBottom: '10px' }}>
              "Who opened this, and when?"
            </h3>
            <p style={{ fontSize: '14px', fontFamily: 'var(--font-body)', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '16px' }}>
              When a document leaks to a competitor or the press, traditional storage logs are either missing,
              mutable, or unable to prove whether an admin or attacker altered timestamps after the incident.
            </p>
            <div style={{
              background: 'var(--blue-surface)',
              border: '1px solid var(--blue-border)',
              borderRadius: 'var(--radius-sm)',
              padding: '10px 14px',
              fontSize: '13px',
              fontFamily: 'var(--font-body)',
              color: 'var(--blue-text)'
            }}>
              <strong>TrustVault Solution:</strong> Append-only SHA-256 cryptographic chain. Every upload, access attempt, download, and revocation is hashed into an immutable ledger.
            </div>
          </div>
        </div>
      </section>

      {/* Core Platform Architecture Features */}
      <section style={{ marginBottom: '64px' }}>
        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <h2 style={{ fontSize: '26px', fontWeight: 800, marginBottom: '8px', letterSpacing: '-0.5px' }}>
            Built With Zero-Trust at Every Layer
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px' }}>
            From cryptographic storage to real-time revocation and AI-assisted governance.
          </p>
        </div>

        <div className="grid-cards">
          {/* 1. Blue: Zero-Trust */}
          <div className="glass-panel">
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '10px',
              background: 'var(--blue-surface)',
              border: '1px solid var(--blue-border)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              🔒
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '8px' }}>
              Zero-Trust & Anti-IDOR Defense
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Unauthorized file requests return strict <code style={{ color: 'var(--blue-text)', background: 'var(--blue-surface)', padding: '2px 5px', borderRadius: '4px' }}>404 Not Found</code> rather than <code>403 Forbidden</code>,
              eliminating metadata leakage and ID enumeration attacks.
            </p>
          </div>

          {/* 2. Green: SHA-256 */}
          <div className="glass-panel">
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '10px',
              background: 'var(--green-surface)',
              border: '1px solid var(--green-border)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              🔗
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '8px' }}>
              SHA-256 Cryptographic Chain
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Each audit log entry calculates <code style={{ color: 'var(--green-text)', background: 'var(--green-surface)', padding: '2px 5px', borderRadius: '4px' }}>currentHash = SHA256(prevHash + action + timestamp + payload)</code>.
              Database tampering is immediately detected by the automated verification engine.
            </p>
          </div>

          {/* 3. Red: Revocation */}
          <div className="glass-panel">
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '10px',
              background: 'var(--rose-surface)',
              border: '1px solid var(--rose-border)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              ⚡
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '8px' }}>
              Immediate Real-Time Revocation
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Revoke recipient access with a single click. Document downloads and previews are terminated
              instantly without relying on stale tokens or caching windows.
            </p>
          </div>

          {/* 4. Yellow/Amber: AI Advisor */}
          <div className="glass-panel">
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '10px',
              background: 'var(--amber-surface)',
              border: '1px solid var(--amber-border)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              🤖
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '8px' }}>
              AI Least-Privilege Advisor
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Automated sensitivity heuristics analyze file titles, categories, and sharing parameters to warn
              against over-privileged grants and recommend minimal expiration policies.
            </p>
          </div>

          {/* 5. Indigo: Private Storage */}
          <div className="glass-panel">
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '10px',
              background: '#eef2ff',
              border: '1px solid #c7d2fe',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              📦
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '8px' }}>
              Private Server-Mediated Storage
            </h4>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              Raw files are stored in private cloud storage and streamed exclusively through authenticated,
              audited Spring Boot proxy streams. Direct storage URLs are never exposed to clients.
            </p>
          </div>

          {/* 6. Slate: Governance */}
          <div className="glass-panel">
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '10px',
              background: '#f1f5f9',
              border: '1px solid #e2e8f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '20px',
              marginBottom: '14px'
            }}>
              👥
            </div>
            <h4 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '8px' }}>
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
          <h2 style={{ fontSize: '26px', fontFamily: 'var(--font-display)', fontWeight: 800, marginBottom: '8px', letterSpacing: '-0.5px' }}>
            How Document Protection Works
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '15px', fontFamily: 'var(--font-body)' }}>
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
              fontFamily: 'var(--font-mono)',
              fontWeight: 800,
              color: 'rgba(15, 23, 42, 0.08)'
            }}>
              01
            </div>
            <h4 style={{ fontSize: '17px', fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '10px', color: 'var(--blue-text)' }}>
              1. Upload & Hash
            </h4>
            <p style={{ fontSize: '14px', fontFamily: 'var(--font-body)', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
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
              fontFamily: 'var(--font-mono)',
              fontWeight: 800,
              color: 'rgba(15, 23, 42, 0.08)'
            }}>
              02
            </div>
            <h4 style={{ fontSize: '17px', fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '10px', color: 'var(--green-text)' }}>
              2. Share With Least Privilege
            </h4>
            <p style={{ fontSize: '14px', fontFamily: 'var(--font-body)', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
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
              fontFamily: 'var(--font-mono)',
              fontWeight: 800,
              color: 'rgba(15, 23, 42, 0.08)'
            }}>
              03
            </div>
            <h4 style={{ fontSize: '17px', fontFamily: 'var(--font-heading)', fontWeight: 700, marginBottom: '10px', color: '#4338ca' }}>
              3. Prove Access & Audit
            </h4>
            <p style={{ fontSize: '14px', fontFamily: 'var(--font-body)', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
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
        background: 'linear-gradient(135deg, #eff6ff 0%, #ffffff 100%)',
        border: '1px solid #bfdbfe',
        boxShadow: '0 4px 12px rgba(37, 99, 235, 0.08)',
        marginBottom: '40px'
      }}>
        <h3 style={{ fontSize: '28px', fontFamily: 'var(--font-display)', fontWeight: 800, marginBottom: '12px', color: 'var(--text-primary)', letterSpacing: '-0.5px' }}>
          Ready to Secure Your Confidential Documents?
        </h3>
        <p style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-body)', maxWidth: '580px', margin: '0 auto 24px', fontSize: '15px' }}>
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
