import { useState } from 'react';
import { extractDocument } from '../api/endpoints';
import type { ExtractedProfileDto } from '../api/types';

export default function DocumentUploadPage() {
  const [file, setFile]         = useState<File | null>(null);
  const [taxYear, setTaxYear]   = useState('2024');
  const [result, setResult]     = useState<ExtractedProfileDto | null>(null);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) return;
    setLoading(true);
    setError(null);
    try {
      const data = await extractDocument(file, taxYear);
      setResult(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1>Document Upload</h1>
      <p className="subtitle">Upload a PDF tax slip (T4, T5, RRSP receipt, T2202) to extract your tax data.</p>

      <form onSubmit={handleSubmit} className="card form-card">
        <div className="field">
          <label>Tax Year</label>
          <input
            type="text"
            value={taxYear}
            onChange={e => setTaxYear(e.target.value)}
            placeholder="2024"
            maxLength={4}
          />
        </div>
        <div className="field">
          <label>PDF Tax Slip</label>
          <input
            type="file"
            accept="application/pdf"
            onChange={e => setFile(e.target.files?.[0] ?? null)}
          />
        </div>
        <button type="submit" disabled={!file || loading} className="btn-primary">
          {loading ? 'Extracting…' : 'Extract'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>

      {result && (
        <div className="card result-card">
          <h2>Extracted Profile</h2>
          <p className="profile-id">Profile ID: <code>{result.profileId}</code></p>
          <table>
            <tbody>
              <tr><td>Tax Year</td><td>{result.taxYear}</td></tr>
              <tr><td>Employment Income</td><td>{fmt(result.employmentIncome)}</td></tr>
              <tr><td>RRSP Contribution</td><td>{fmt(result.rrspContribution)}</td></tr>
              <tr><td>Capital Gains</td><td>{fmt(result.capitalGains)}</td></tr>
              <tr><td>RRSP Room</td><td>{fmt(result.rrspContributionRoom)}</td></tr>
              <tr><td>Capital Loss C/F</td><td>{fmt(result.capitalLossCarryforward)}</td></tr>
              <tr><td>Tuition C/F</td><td>{fmt(result.tuitionCarryforward)}</td></tr>
            </tbody>
          </table>
          <h3>Confidence Scores</h3>
          <table>
            <tbody>
              {Object.entries(result.confidenceScores).map(([k, v]) => (
                <tr key={k}>
                  <td>{camel(k)}</td>
                  <td><ConfidenceBar value={v as number} /></td>
                </tr>
              ))}
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

function camel(s: string) {
  return s.replace(/([A-Z])/g, ' $1').replace(/^./, c => c.toUpperCase());
}

function ConfidenceBar({ value }: { value: number }) {
  const pct   = Math.round(value * 100);
  const color = pct >= 80 ? '#22c55e' : pct >= 50 ? '#f59e0b' : '#ef4444';
  return (
    <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <span style={{
        display: 'inline-block', width: 80, height: 8,
        background: '#e5e7eb', borderRadius: 4, overflow: 'hidden',
      }}>
        <span style={{ display: 'block', width: `${pct}%`, height: '100%', background: color }} />
      </span>
      <span style={{ fontSize: 12, color }}>{pct}%</span>
    </span>
  );
}
