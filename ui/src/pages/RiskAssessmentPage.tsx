import { useState } from 'react';
import { assessRisk } from '../api/endpoints';
import type { RiskAssessmentRequestDto, RiskResponseDto } from '../api/types';

const EMPTY: RiskAssessmentRequestDto = {
  profileId: '', taxYear: '2024',
  employmentIncome: 0, rrspContribution: 0, capitalGains: 0,
  rrspContributionRoom: 0, capitalLossCarryforward: 0, tuitionCarryforward: 0,
  priorYearIncome: 0, hasDiscrepancy: false,
};

export default function RiskAssessmentPage() {
  const [form, setForm]       = useState<RiskAssessmentRequestDto>(EMPTY);
  const [result, setResult]   = useState<RiskResponseDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const setStr = (k: keyof RiskAssessmentRequestDto) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [k]: e.target.value }));

  const setNum = (k: keyof RiskAssessmentRequestDto) =>
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm(f => ({ ...f, [k]: parseFloat(e.target.value) || 0 }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      setResult(await assessRisk(form));
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Risk assessment failed');
    } finally {
      setLoading(false);
    }
  };

  const riskColor: Record<string, string> = { LOW: '#22c55e', MEDIUM: '#f59e0b', HIGH: '#ef4444' };

  return (
    <div className="page">
      <h1>Risk Assessment</h1>
      <p className="subtitle">Evaluate pre-filing CRA reassessment risk for this tax profile.</p>

      <form onSubmit={handleSubmit} className="card form-card">
        <div className="field-row">
          <div className="field"><label>Profile ID</label>
            <input value={form.profileId} onChange={setStr('profileId')} /></div>
          <div className="field"><label>Tax Year</label>
            <input value={form.taxYear} onChange={setStr('taxYear')} maxLength={4} /></div>
        </div>
        <div className="field-row">
          <NumField label="Employment Income"   value={form.employmentIncome}        onChange={setNum('employmentIncome')} />
          <NumField label="RRSP Contribution"   value={form.rrspContribution}        onChange={setNum('rrspContribution')} />
          <NumField label="Capital Gains"       value={form.capitalGains}            onChange={setNum('capitalGains')} />
        </div>
        <div className="field-row">
          <NumField label="RRSP Room"           value={form.rrspContributionRoom}    onChange={setNum('rrspContributionRoom')} />
          <NumField label="Capital Loss C/F"    value={form.capitalLossCarryforward} onChange={setNum('capitalLossCarryforward')} />
          <NumField label="Prior Year Income"   value={form.priorYearIncome}         onChange={setNum('priorYearIncome')} />
        </div>
        <div className="field checkbox-field">
          <label>
            <input
              type="checkbox"
              checked={form.hasDiscrepancy}
              onChange={e => setForm(f => ({ ...f, hasDiscrepancy: e.target.checked }))}
            />
            {' '}Prior reconciliation found discrepancies
          </label>
        </div>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Assessing…' : 'Assess Risk'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>

      {result && (
        <div className="card result-card">
          <h2>Risk Result</h2>
          <div className="risk-banner" style={{ borderColor: riskColor[result.riskLevel] }}>
            <div>
              <span className="risk-level" style={{ color: riskColor[result.riskLevel] }}>
                {result.riskLevel}
              </span>
              <span className="risk-score">Score: {result.riskScore} / 100</span>
            </div>
            <div className="risk-bar-outer">
              <div className="risk-bar-inner" style={{
                width: `${result.riskScore}%`,
                background: riskColor[result.riskLevel],
              }} />
            </div>
          </div>
          {result.drivers.length > 0 && (
            <>
              <h3>Risk Drivers</h3>
              <ul className="drivers-list">
                {result.drivers.map((d, i) => <li key={i}>{d}</li>)}
              </ul>
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
