import { NavLink, Outlet, useLocation } from 'react-router-dom';

const NAV = [
  { to: '/',           icon: '🏠', label: 'Dashboard' },
  { to: '/upload',     icon: '📄', label: 'Upload Document' },
  { to: '/cra',        icon: '🏛', label: 'CRA Import' },
  { to: '/reconcile',  icon: '🔍', label: 'Reconciliation' },
  { to: '/optimize',   icon: '📈', label: 'Optimization' },
  { to: '/risk',       icon: '⚠',  label: 'Risk Assessment' },
  { to: '/provincial', icon: '🗺', label: 'Provincial Policy' },
  { to: '/review',     icon: '👁', label: 'Review & Audit' },
];

function usePageTitle() {
  const { pathname } = useLocation();
  const match = NAV.find(n => n.to === pathname) ?? NAV[0];
  return match.label;
}

export default function Layout() {
  const pageTitle = usePageTitle();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink to="/" className="sidebar-brand" style={{ textDecoration: 'none' }}>
          <div className="brand-icon-wrap">🍁</div>
          <div className="brand-text">
            <span className="brand-name">ATIE</span>
            <span className="brand-sub">Tax Intelligence</span>
          </div>
        </NavLink>

        <nav>
          <p className="nav-section-label">Modules</p>
          {NAV.map(({ to, icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              <span className="nav-icon" aria-hidden="true">{icon}</span>
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <p className="sidebar-footer-label">v1.0.0 · Canada</p>
        </div>
      </aside>

      <div className="main-content">
        <header className="topbar">
          <span className="topbar-breadcrumb">
            ATIE &rsaquo; <strong>{pageTitle}</strong>
          </span>
        </header>
        <Outlet />
        <footer className="app-footer">
          <div className="app-footer-left">
            <div className="app-footer-brand">
              <span className="footer-maple" aria-hidden="true">🍁</span>
              ATIE
            </div>
            <span className="app-footer-sep" aria-hidden="true">·</span>
            <span className="app-footer-tagline">AI constructs. Human supervises. System files.</span>
          </div>
          <div className="app-footer-right">
            <span className="app-footer-pill">
              <span aria-hidden="true">●</span> Spring Boot · Java 21
            </span>
            <span className="app-footer-sep" aria-hidden="true">·</span>
            <span className="app-footer-link">v1.0.0</span>
            <span className="app-footer-sep" aria-hidden="true">·</span>
            <span className="app-footer-link">Canada 🇨🇦</span>
          </div>
        </footer>
      </div>
    </div>
  );
}
