import { useState } from 'react';
import { simulateRrsp } from '../api/endpoints';
import type { TaxProfileBody, OptimizationResponseDto } from '../api/types';

const EMPTY: TaxProfileBody = {
  profileId: '', taxYear: '2024',
  employmentIncome: 0, rrspContribution: 0, capitalGains: 0,
  rrspContributionRoom: 0, capitalLossCarryforward: 0, tuitionCarryforward: 0,
  priorYearIncome: 0,
};

export default function OptimizationPage() {
  const [profile, setProfile]     = useState<TaxProfileBody>(EMPTY);
  const [contrib, setContrib]     = useState<number>(5000);
  const [result, setResult]       = useState<OptimizationResponseDto | null>(null);
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState<string | null>(null);

  const setStr = (k: keyof TaxProfileBody) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setProfile(p => ({ ...p, [k]: e.target.value }));

  const setNum = (k: keyof TaxProfileBody) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setProfile(p => ({ ...p, [k]: parseFloat(e.target.value) || 0 }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      setResult(await simulateRrsp(profile, contrib));
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Optimization failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>RRSP Optimization</h1>
      <p className="subtitle">Simulate the tax impact of an additional RRSP contribution.</p>

      <form onSubmit={handleSubmit} className="card form-card">
        <div className="field-row">
          <div className="field"><label>Profile ID</label>
            <input value={profile.profileId} onChange={setStr('profileId')} /></div>
          <div className="field"><label>Tax Year</label>
            <input value={profile.taxYear} onChange={setStr('taxYear')} maxLength={4} /></div>
        </div>
        <div className="field-row">
          <NumField label="Employment Income"   value={profile.employmentIncome}        onChange={setNum('employmentIncome')} />
          <NumField label="RRSP Contribution"   value={profile.rrspContribution}        onChange={setNum('rrspContribution')} />
          <NumField label="Capital Gains"       value={profile.capitalGains}            onChange={setNum('capitalGains')} />
        </div>
        <div className="field-row">
          <NumField label="RRSP Room"           value={profile.rrspContributionRoom}    onChange={setNum('rrspContributionRoom')} />
          <NumField label="Capital Loss C/F"    value={profile.capitalLossCarryforward} onChange={setNum('capitalLossCarryforward')} />
          <NumField label="Tuition C/F"         value={profile.tuitionCarryforward}     onChange={setNum('tuitionCarryforward')} />
        </div>
        <div className="field">
          <label>Additional RRSP Contribution to Simulate ($)</label>
          <input type="number" step="100" min="0" value={contrib}
            onChange={e => setContrib(parseFloat(e.target.value) || 0)} />
        </div>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Simulating…' : 'Simulate'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>

      {result && (
        <div className="card result-card">
          <h2>Optimization Result</h2>
          <div className="savings-banner">
            <span>Estimated Savings</span>
            <strong style={{ color: result.estimatedSavings > 0 ? '#22c55e' : '#6b7280' }}>
              {fmt(result.estimatedSavings)}
            </strong>
          </div>
          <table>
            <tbody>
              <tr><td>Additional Contribution</td><td>{fmt(result.additionalContribution)}</td></tr>
              <tr><td>Current Tax</td><td>{fmt(result.currentTax)}</td></tr>
              <tr><td>Optimized Tax</td><td>{fmt(result.optimizedTax)}</td></tr>
              <tr><td>Capital Loss C/F Applied</td><td>{fmt(result.capitalLossCarryforwardApplied)}</td></tr>
              <tr><td>Tuition Credit Applied</td><td>{fmt(result.tuitionCreditApplied)}</td></tr>
            </tbody>
          </table>
          <div className="ai-box">
            <h3>AI Analysis</h3>
            <p>{result.aiTradeoffExplanation}</p>
          </div>
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
