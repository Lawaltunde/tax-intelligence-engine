import { useState } from 'react';
import { importFromCra } from '../api/endpoints';
import type { CraImportDto } from '../api/types';

export default function CraImportPage() {
  const [form, setForm]       = useState({ sin: '', dateOfBirth: '', netfileAccessCode: '', taxYear: '2024' });
  const [showSin, setShowSin] = useState(false);
  const [result, setResult]   = useState<CraImportDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await importFromCra(form);
      setResult(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'CRA import failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>CRA Import</h1>
      <p className="subtitle">
        Pull your pre-populated T1 slip data directly from the CRA Auto-fill My Return service.
        In mock mode, any valid-format credentials return sample data.
      </p>

      <form onSubmit={handleSubmit} className="card form-card">
        <div className="field">
          <label htmlFor="cra-sin">SIN (9 digits, no dashes)</label>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <input
              id="cra-sin"
              type={showSin ? 'text' : 'password'}
              value={form.sin}
              onChange={set('sin')}
              placeholder="123456789"
              maxLength={9}
              inputMode="numeric"
              pattern="\d{9}"
              autoComplete="off"
            />
            <button
              type="button"
              onClick={() => setShowSin(s => !s)}
              style={{ padding: '4px 10px', fontSize: '0.8rem', cursor: 'pointer',
                       border: '1px solid #d1d5db', borderRadius: 6, background: '#f9fafb' }}
              aria-label={showSin ? 'Hide SIN' : 'Show SIN'}
            >
              {showSin ? 'Hide' : 'Show'}
            </button>
          </div>
        </div>
        <div className="field">
          <label htmlFor="cra-dob">Date of Birth</label>
          <input id="cra-dob" type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} />
        </div>
        <div className="field">
          <label htmlFor="cra-nac">NETFILE Access Code (4 characters)</label>
          <input id="cra-nac" type="text" value={form.netfileAccessCode} onChange={set('netfileAccessCode')} placeholder="AB12" maxLength={4} />
        </div>
        <div className="field">
          <label htmlFor="cra-taxYear">Tax Year</label>
          <input id="cra-taxYear" type="text" value={form.taxYear} onChange={set('taxYear')} placeholder="2024" maxLength={4} />
        </div>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Importing…' : 'Import from CRA'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>

      {result && (
        <div className="card result-card">
          <h2>CRA Data — {result.taxYear}</h2>
          <p className="tag">{result.dataSource === 'MOCK' ? '⚠ Mock Data' : '✓ Live CRA Data'}</p>
          <table>
            <tbody>
              <tr><td>Employer</td><td>{result.employerName}</td></tr>
              <tr><td>Employment Income</td><td>{fmt(result.employmentIncome)}</td></tr>
              <tr><td>Income Tax Deducted</td><td>{fmt(result.incomeTaxDeducted)}</td></tr>
              <tr><td>RRSP Contribution</td><td>{fmt(result.rrspContribution)}</td></tr>
              <tr><td>RRSP Deduction Limit</td><td>{fmt(result.rrspDeductionLimit)}</td></tr>
              <tr><td>Capital Gains</td><td>{fmt(result.capitalGains)}</td></tr>
              <tr><td>Dividend Income</td><td>{fmt(result.dividendIncome)}</td></tr>
              <tr><td>Other Income</td><td>{fmt(result.otherIncome)}</td></tr>
              <tr><td>SIN (last 3)</td><td>***-***-{result.sinLast3}</td></tr>
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function fmt(n: number) {
  return new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' }).format(n);
}
