import { NavLink, Outlet } from 'react-router-dom';

const NAV = [
  { to: '/',             label: '🏠 Dashboard' },
  { to: '/upload',       label: '📄 Upload Document' },
  { to: '/cra',          label: '🏛 CRA Import' },
  { to: '/reconcile',    label: '🔍 Reconciliation' },
  { to: '/optimize',     label: '📈 Optimization' },
  { to: '/risk',         label: '⚠ Risk Assessment' },
  { to: '/provincial',   label: '🗺 Provincial Policy' },
  { to: '/review',       label: '👁 Review & Audit' },
];

export default function Layout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">🍁</span>
          <span className="brand-name">ATIE</span>
        </div>
        <nav>
          {NAV.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
