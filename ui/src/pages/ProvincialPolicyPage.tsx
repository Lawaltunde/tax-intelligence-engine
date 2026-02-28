import { useState } from 'react';
import { analyseProvincialPolicy } from '../api/endpoints';
import type { ProvincialPolicyRequest, ProvincialPolicyResponse, Province } from '../api/types';

const PROVINCES: { code: Province; name: string }[] = [
  { code: 'AB', name: 'Alberta' },
  { code: 'BC', name: 'British Columbia' },
  { code: 'MB', name: 'Manitoba' },
  { code: 'NB', name: 'New Brunswick' },
  { code: 'NL', name: 'Newfoundland and Labrador' },
  { code: 'NS', name: 'Nova Scotia' },
  { code: 'NT', name: 'Northwest Territories' },
  { code: 'NU', name: 'Nunavut' },
  { code: 'ON', name: 'Ontario' },
  { code: 'PE', name: 'Prince Edward Island' },
  { code: 'QC', name: 'Quebec' },
  { code: 'SK', name: 'Saskatchewan' },
  { code: 'YT', name: 'Yukon' },
];

const EMPTY: ProvincialPolicyRequest = {
  profileId: '', province: 'ON', taxYear: '2024',
  employmentIncome: 0, rrspContribution: 0, capitalGains: 0,
  tuitionAmount: 0, hasDependents: false, isStudent: false,
};

export default function ProvincialPolicyPage() {
  const [form, setForm]       = useState<ProvincialPolicyRequest>(EMPTY);
  const [result, setResult]   = useState<ProvincialPolicyResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const setStr = (k: keyof ProvincialPolicyRequest) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
      setForm(f => ({ ...f, [k]: e.target.value }));

  const setNum = (k: keyof ProvincialPolicyRequest) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [k]: parseFloat(e.target.value) || 0 }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      setResult(await analyseProvincialPolicy(form));
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Provincial analysis failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Provincial Policy</h1>
      <p className="subtitle">Analyse provincial tax, available credits, and AI-generated recommendations.</p>

      <form onSubmit={handleSubmit} className="card form-card">
        <div className="field-row">
          <div className="field"><label>Profile ID</label>
            <input value={form.profileId} onChange={setStr('profileId')} /></div>
          <div className="field"><label>Tax Year</label>
            <input value={form.taxYear} onChange={setStr('taxYear')} maxLength={4} /></div>
          <div className="field"><label>Province</label>
            <select value={form.province} onChange={setStr('province')}>
              {PROVINCES.map(p => <option key={p.code} value={p.code}>{p.name}</option>)}
            </select>
          </div>
        </div>
        <div className="field-row">
          <NumField label="Employment Income"   value={form.employmentIncome}  onChange={setNum('employmentIncome')} />
          <NumField label="RRSP Contribution"   value={form.rrspContribution}  onChange={setNum('rrspContribution')} />
          <NumField label="Capital Gains"       value={form.capitalGains}      onChange={setNum('capitalGains')} />
          <NumField label="Tuition Amount"      value={form.tuitionAmount}     onChange={setNum('tuitionAmount')} />
        </div>
        <div className="field-row checkbox-row">
          <label>
            <input type="checkbox" checked={form.hasDependents}
              onChange={e => setForm(f => ({ ...f, hasDependents: e.target.checked }))} />
            {' '}Has Dependents
          </label>
          <label>
            <input type="checkbox" checked={form.isStudent}
              onChange={e => setForm(f => ({ ...f, isStudent: e.target.checked }))} />
            {' '}Is Student
          </label>
        </div>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Analysing…' : 'Analyse'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>

      {result && (
        <div className="card result-card">
          <h2>Provincial Tax Analysis — {result.province}</h2>
          <table>
            <tbody>
              <tr><td>Provincial Tax Payable</td><td>{fmt(result.provincialTaxPayable)}</td></tr>
              <tr><td>Estimated Credits Total</td><td style={{ color: '#22c55e' }}>−{fmt(result.estimatedCreditsTotal)}</td></tr>
              <tr className="total-row"><td><strong>Net Provincial Tax</strong></td><td><strong>{fmt(result.netProvincialTax)}</strong></td></tr>
            </tbody>
          </table>

          {result.eligibleCredits.length > 0 && (
            <>
              <h3>Eligible Credits</h3>
              <ul>{result.eligibleCredits.map((c, i) => <li key={i}>{c}</li>)}</ul>
            </>
          )}

          <div className="ai-box">
            <h3>Policy Explanation</h3>
            <p>{result.policyExplanation}</p>
          </div>

          {result.recommendations.length > 0 && (
            <>
              <h3>Recommendations</h3>
              <ol>{result.recommendations.map((r, i) => <li key={i}>{r}</li>)}</ol>
            </>
          )}
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

function fmt(n: number) {
  return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(n);
}
