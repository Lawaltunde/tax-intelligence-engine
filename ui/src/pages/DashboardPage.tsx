export default function DashboardPage() {
  const steps = [
    { num: '01', icon: '📄', title: 'Upload Document',   desc: 'AI extracts structured tax fields from your PDF slip with confidence scoring.', to: '/upload' },
    { num: '02', icon: '🏛', title: 'CRA Import',        desc: 'Pull your pre-populated T1 data directly from CRA records.',                    to: '/cra' },
    { num: '03', icon: '🔍', title: 'Reconciliation',    desc: 'AI detects mismatches, explains causes, and flags items for human review.',      to: '/reconcile' },
    { num: '04', icon: '📈', title: 'RRSP Optimization', desc: 'Simulate contribution strategies and quantify your tax savings.',                to: '/optimize' },
    { num: '05', icon: '⚠',  title: 'Risk Assessment',   desc: 'AI scores reassessment likelihood and identifies high-judgment fields.',         to: '/risk' },
    { num: '06', icon: '🗺', title: 'Provincial Policy', desc: 'Analyse province-specific credits and applicable tax policy.',                   to: '/provincial' },
    { num: '07', icon: '👁', title: 'Review & Audit',    desc: 'Human review queue with full AI decision trail and audit log.',                  to: '/review' },
  ];

  return (
    <div className="page">
      <div className="dashboard-hero">
        <div className="dashboard-hero-content">
          <p className="hero-eyebrow">Autonomous Tax Intelligence Engine</p>
          <h1>AI constructs.<br /><span>Human supervises.</span><br />System files.</h1>
          <p>
            An end-to-end Canadian tax workflow rebuilt from the ground up with AI —
            deterministic logic where certainty is required, LLM reasoning where judgment is needed.
          </p>
        </div>
      </div>

      <p className="dashboard-section-label">Workflow — follow these steps in order</p>
      <div className="dashboard-grid">
        {steps.map(c => (
          <a key={c.to} href={c.to} className="dash-card">
            <div className="dash-card-header">
              <div className="dash-icon-wrap" aria-hidden="true">{c.icon}</div>
              <span className="dash-step-num">{c.num}</span>
            </div>
            <h3>{c.title}</h3>
            <p>{c.desc}</p>
            <span className="dash-card-arrow" aria-hidden="true">→</span>
          </a>
        ))}
      </div>

      <div className="atie-principles">
        <div className="atie-principle">
          <span className="principle-icon">⚙</span>
          <strong>Deterministic Logic</strong>
          <span>Numeric calculations, eligibility thresholds, tax bracket rules</span>
        </div>
        <div className="atie-principle-divider" aria-hidden="true">+</div>
        <div className="atie-principle">
          <span className="principle-icon">✦</span>
          <strong>LLM Reasoning</strong>
          <span>Document extraction, discrepancy explanation, risk hypothesis</span>
        </div>
        <div className="atie-principle-divider" aria-hidden="true">=</div>
        <div className="atie-principle">
          <span className="principle-icon">🛡</span>
          <strong>Explainable & Auditable</strong>
          <span>Every AI decision is logged, scored, and reviewable by a human</span>
        </div>
      </div>
    </div>
  );
}
