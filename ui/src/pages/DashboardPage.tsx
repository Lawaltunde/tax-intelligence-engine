export default function DashboardPage() {
  const cards = [
    { icon: '📄', title: 'Upload Document',   desc: 'Extract tax fields from a PDF slip using AI.', to: '/upload' },
    { icon: '🏛', title: 'CRA Import',        desc: 'Pull your pre-populated T1 data from CRA.',   to: '/cra' },
    { icon: '🔍', title: 'Reconciliation',    desc: 'Compare uploaded data against CRA records.',  to: '/reconcile' },
    { icon: '📈', title: 'RRSP Optimization', desc: 'Simulate additional RRSP contributions.',     to: '/optimize' },
    { icon: '⚠',  title: 'Risk Assessment',   desc: 'Evaluate pre-filing reassessment risk.',      to: '/risk' },
    { icon: '🗺', title: 'Provincial Policy', desc: 'Analyse provincial tax and eligible credits.', to: '/provincial' },
    { icon: '👁', title: 'Review & Audit',    desc: 'Human review queue and AI decision trail.',   to: '/review' },
  ];

  return (
    <div className="page">
      <div className="dashboard-hero">
        <div className="dashboard-hero-content">
          <h1>AI Tax <span>Intelligence</span> Engine</h1>
          <p>
            Canadian tax AI assistant — document ingestion, CRA reconciliation,
            RRSP optimization, risk assessment, and provincial policy analysis.
          </p>
          <div className="hero-stats">
            <div className="hero-stat">
              <span className="hero-stat-value">7</span>
              <span className="hero-stat-label">AI Modules</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">GPT-4o</span>
              <span className="hero-stat-label">AI Model</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">Mock</span>
              <span className="hero-stat-label">CRA Mode</span>
            </div>
            <div className="hero-stat">
              <span className="hero-stat-value">CA</span>
              <span className="hero-stat-label">Tax Jurisdiction</span>
            </div>
          </div>
        </div>
      </div>

      <p className="dashboard-section-label">Available Modules</p>
      <div className="dashboard-grid">
        {cards.map(c => (
          <a key={c.to} href={c.to} className="dash-card">
            <div className="dash-icon-wrap" aria-hidden="true">{c.icon}</div>
            <h3>{c.title}</h3>
            <p>{c.desc}</p>
            <span className="dash-card-arrow" aria-hidden="true">→</span>
          </a>
        ))}
      </div>
    </div>
  );
}
