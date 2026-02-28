import { useState } from 'react';
import { importFromCra } from '../api/endpoints';
import type { CraImportDto } from '../api/types';

export default function CraImportPage() {
  const [form, setForm]       = useState({ sin: '', dateOfBirth: '', netfileAccessCode: '', taxYear: '2024' });
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
          <label>SIN (9 digits, no dashes)</label>
          <input type="text" value={form.sin} onChange={set('sin')} placeholder="123456789" maxLength={9} />
        </div>
        <div className="field">
          <label>Date of Birth</label>
          <input type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} />
        </div>
        <div className="field">
          <label>NETFILE Access Code (4 characters)</label>
          <input type="text" value={form.netfileAccessCode} onChange={set('netfileAccessCode')} placeholder="AB12" maxLength={4} />
        </div>
        <div className="field">
          <label>Tax Year</label>
          <input type="text" value={form.taxYear} onChange={set('taxYear')} placeholder="2024" maxLength={4} />
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
