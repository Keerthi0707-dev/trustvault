import React from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand" title="TrustVault Home">
          <div className="brand-icon">TV</div>
          <span>TrustVault</span>
        </Link>

        {user ? (
          <nav className="nav-links">
            <NavLink to="/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              Dashboard
            </NavLink>
            <NavLink to="/documents" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              Documents
            </NavLink>
            <NavLink to="/activity" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              Activity
            </NavLink>
            {isAdmin && (
              <>
                <NavLink to="/admin" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                  Users
                </NavLink>
                <NavLink to="/admin/audit" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                  Audit & Integrity
                </NavLink>
              </>
            )}
          </nav>
        ) : (
          <nav className="nav-links">
            <NavLink to="/" end className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              Platform Overview
            </NavLink>
          </nav>
        )}

        <div className="nav-user">
          {user ? (
            <>
              <span className={`badge badge-${user.role.toLowerCase()}`}>
                {user.role}
              </span>
              <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                {user.email}
              </span>
              <button onClick={handleLogout} className="btn btn-secondary btn-sm" title="Sign out">
                Sign out
              </button>
            </>
          ) : (
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              <Link to="/login" className="btn btn-secondary btn-sm">
                Sign In
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
