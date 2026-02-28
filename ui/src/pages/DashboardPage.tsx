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
      <h1>AI Tax Intelligence Engine</h1>
      <p className="subtitle">
        Canadian tax AI assistant — document ingestion, CRA reconciliation,
        RRSP optimization, risk assessment, and provincial policy analysis.
      </p>
      <div className="dashboard-grid">
        {cards.map(c => (
          <a key={c.to} href={c.to} className="dash-card">
            <span className="dash-icon" aria-hidden="true">{c.icon}</span>
            <h3>{c.title}</h3>
            <p>{c.desc}</p>
          </a>
        ))}
      </div>
    </div>
  );
}
