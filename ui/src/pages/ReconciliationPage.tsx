import { useState } from 'react';
import { runReconciliation } from '../api/endpoints';
import type { ReconciliationRequestDto, ReconciliationResponseDto } from '../api/types';

const EMPTY: ReconciliationRequestDto = {
  profileId: '', sin: '', taxYear: '2024',
  employmentIncome: 0, rrspContribution: 0, capitalGains: 0,
  rrspContributionRoom: 0, capitalLossCarryforward: 0, tuitionCarryforward: 0,
  priorYearIncome: 0,
};

export default function ReconciliationPage() {
  const [form, setForm]       = useState<ReconciliationRequestDto>(EMPTY);
  const [results, setResults] = useState<ReconciliationResponseDto[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const setStr = (k: keyof ReconciliationRequestDto) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [k]: e.target.value }));

  const setNum = (k: keyof ReconciliationRequestDto) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [k]: parseFloat(e.target.value) || 0 }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      setResults(await runReconciliation(form));
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Reconciliation failed');
    } finally {
      setLoading(false);
    }
  };

  const hasFinding = results && results.length > 0;

  return (
    <div className="page">
      <h1>Reconciliation</h1>
      <p className="subtitle">Compare your uploaded tax data against CRA-reported figures.</p>

      <form onSubmit={handleSubmit} className="card form-card">
        <div className="field-row">
          <div className="field"><label>Profile ID</label>
            <input value={form.profileId} onChange={setStr('profileId')} placeholder="From document upload" /></div>
          <div className="field"><label>SIN (9 digits)</label>
            <input value={form.sin} onChange={setStr('sin')} placeholder="123456789" maxLength={9} /></div>
          <div className="field"><label>Tax Year</label>
            <input value={form.taxYear} onChange={setStr('taxYear')} maxLength={4} /></div>
        </div>
        <div className="field-row">
          <NumField label="Employment Income"         value={form.employmentIncome}        onChange={setNum('employmentIncome')} />
          <NumField label="RRSP Contribution"         value={form.rrspContribution}        onChange={setNum('rrspContribution')} />
          <NumField label="Capital Gains"             value={form.capitalGains}            onChange={setNum('capitalGains')} />
        </div>
        <div className="field-row">
          <NumField label="RRSP Room"                 value={form.rrspContributionRoom}    onChange={setNum('rrspContributionRoom')} />
          <NumField label="Capital Loss C/F"          value={form.capitalLossCarryforward} onChange={setNum('capitalLossCarryforward')} />
          <NumField label="Tuition C/F"               value={form.tuitionCarryforward}     onChange={setNum('tuitionCarryforward')} />
        </div>
        <div className="field-row">
          <NumField label="Prior Year Income"         value={form.priorYearIncome}         onChange={setNum('priorYearIncome')} />
        </div>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Reconciling…' : 'Run Reconciliation'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>

      {results !== null && (
        <div className="card result-card">
          <h2>Results</h2>
          {!hasFinding
            ? <p className="success">✓ No discrepancies found — your data matches CRA records.</p>
            : (
              <table className="results-table">
                <thead>
                  <tr>
                    <th>Field</th><th>Uploaded</th><th>CRA</th><th>Delta</th>
                    <th>Severity</th><th>Review?</th><th>AI Explanation</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((r, i) => (
                    <tr key={i} className={`sev-${r.severity.toLowerCase()}`}>
                      <td>{r.field}</td>
                      <td>{fmt(r.uploadedValue)}</td>
                      <td>{fmt(r.craValue)}</td>
                      <td>{fmt(r.delta)}</td>
                      <td><SeverityBadge s={r.severity} /></td>
                      <td>{r.requiresReview ? '⚠ Yes' : '—'}</td>
                      <td className="explanation">{r.aiExplanation}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
          }
        </div>
      )}
    </div>
  );
}

function NumField({ label, value, onChange }: { label: string; value: number; onChange: React.ChangeEventHandler<HTMLInputElement> }) {
  return (
    <div className="field">
      <label>{label}</label>
      <input type="number" step="0.01" value={value} onChange={onChange} />
    </div>
  );
}

function SeverityBadge({ s }: { s: string }) {
  const colors: Record<string, string> = { LOW: '#22c55e', MEDIUM: '#f59e0b', HIGH: '#ef4444' };
  return <span style={{ color: colors[s] ?? '#6b7280', fontWeight: 600 }}>{s}</span>;
}

function fmt(n: number) {
  return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(n);
}
