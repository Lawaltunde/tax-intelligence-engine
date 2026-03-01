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
          <h1>Canadian Tax <span>Intelligence</span></h1>
          <p>
            Your end-to-end tax workflow — from document ingestion and CRA
            reconciliation through to risk assessment and provincial analysis.
          </p>
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
