import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { extractDocument } from '../api/endpoints';
import type { ExtractedProfileDto } from '../api/types';
import { CardSkeleton } from '../components/Skeleton';
import InlineMessage from '../components/InlineMessage';

export default function DocumentUploadPage() {
  const [files, setFiles]     = useState<File[]>([]);
  const [taxYear, setTaxYear] = useState('2024');

  const { mutate, data: result, isPending, isError, error, reset } =
    useMutation<ExtractedProfileDto, Error, { files: File[]; taxYear: string }>({
      mutationFn: ({ files, taxYear }) => extractDocument(files, taxYear),
      onSuccess: (_data, { files }) => {
        toast.success(
          files.length > 1
            ? `${files.length} slips merged successfully.`
            : 'Document extracted successfully.'
        );
      },
      onError: (err) => {
        toast.error(`Extraction failed: ${err.message}`);
      },
    });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const picked = Array.from(e.target.files ?? []);

    const nonPdf = picked.filter(f => f.type !== 'application/pdf');
    if (nonPdf.length > 0) {
      toast.error(
        `Only PDF files are accepted. Rejected: ${nonPdf.map(f => f.name).join(', ')}`
      );
    }

    const validPdfs = picked.filter(f => f.type === 'application/pdf');
    setFiles(prev => {
      const existingKeys = new Set(prev.map(f => `${f.name}-${f.size}-${f.lastModified}`));
      const fresh = validPdfs.filter(f => !existingKeys.has(`${f.name}-${f.size}-${f.lastModified}`));
      return [...prev, ...fresh];
    });
    e.target.value = '';
  };

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (files.length === 0) return;
    reset();
    mutate({ files, taxYear });
  };

  return (
    <div className="page">
      <h1>Document Upload</h1>
      <p className="subtitle">
        Upload one or more PDF tax slips (T4, T5, RRSP receipt, T2202).
        Multiple T4s from different employers are supported — income will be summed accurately.
      </p>

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
          <label>PDF Tax Slip(s)</label>
          <input
            type="file"
            accept="application/pdf"
            multiple
            onChange={handleFileChange}
          />
          <span style={{ fontSize: 12, color: 'var(--text-muted, #6b7280)', marginTop: 4 }}>
            You can add multiple files one at a time or select several at once.
          </span>
        </div>

        {files.length > 0 && (
          <div className="field">
            <label>
              Selected slip{files.length > 1 ? 's' : ''}&nbsp;
              <span style={{ fontWeight: 400, color: 'var(--text-muted, #6b7280)' }}>
                ({files.length})
              </span>
            </label>
            <ul style={{ margin: 0, padding: 0, listStyle: 'none', display: 'flex', flexDirection: 'column', gap: 6 }}>
              {files.map((f, i) => (
                <li
                  key={f.name}
                  style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    gap: 8, background: 'var(--surface-alt, #f9fafb)',
                    borderRadius: 6, padding: '6px 10px', fontSize: 13,
                  }}
                >
                  <span>📄 {f.name}</span>
                  <button
                    type="button"
                    aria-label={`Remove ${f.name}`}
                    onClick={() => removeFile(i)}
                    style={{
                      background: 'none', border: 'none', cursor: 'pointer',
                      color: '#ef4444', fontSize: 16, lineHeight: 1,
                    }}
                  >
                    ×
                  </button>
                </li>
              ))}
            </ul>
          </div>
        )}

        <button type="submit" disabled={files.length === 0 || isPending} className="btn-primary">
          {isPending
            ? 'Extracting…'
            : files.length > 1
              ? `Extract & Merge ${files.length} Slips`
              : 'Extract'}
        </button>
        {isError && <InlineMessage variant="error">{error.message}</InlineMessage>}
      </form>

      {isPending && <CardSkeleton />}

      {result && (
        <div className="card result-card">
          <h2>
            {files.length > 1 ? `Merged Profile (${files.length} slips)` : 'Extracted Profile'}
          </h2>
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
          {files.length > 1 && (
            <p style={{ fontSize: 12, color: 'var(--text-muted, #6b7280)', marginTop: 0 }}>
              Scores show the lowest confidence across all {files.length} slips — the most conservative reading.
            </p>
          )}
          <table>
            <tbody>
              {Object.entries(
                typeof result.confidenceScores === 'object' && result.confidenceScores != null
                  ? result.confidenceScores
                  : {}
              ).map(([k, v]) => {
                const score = Number(v);
                return (
                  <tr key={k}>
                    <td>{camel(k)}</td>
                    <td><ConfidenceBar value={isFinite(score) ? score : 0} /></td>
                  </tr>
                );
              })}
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
