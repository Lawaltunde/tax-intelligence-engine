import { useState } from 'react';
import { getAllPendingReview, getAuditTrail } from '../api/endpoints';
import type { ReviewItemDto, AuditLogDto, DecisionType } from '../api/types';

const DECISION_TYPES: DecisionType[] = [
  'RECONCILIATION', 'OPTIMIZATION', 'RISK_ASSESSMENT', 'PROVINCIAL_POLICY', 'EXTRACTION',
];

export default function ReviewPage() {
  const [tab, setTab]                     = useState<'pending' | 'audit'>('pending');
  const [pendingItems, setPendingItems]   = useState<ReviewItemDto[] | null>(null);
  const [auditItems, setAuditItems]       = useState<AuditLogDto[] | null>(null);
  const [auditProfileId, setAuditProfileId] = useState('');
  const [auditType, setAuditType]         = useState<DecisionType | ''>('');
  const [loading, setLoading]             = useState(false);
  const [error, setError]                 = useState<string | null>(null);

  const loadPending = async () => {
    setLoading(true); setError(null);
    try { setPendingItems(await getAllPendingReview()); }
    catch (e: unknown) { setError(e instanceof Error ? e.message : 'Failed'); }
    finally { setLoading(false); }
  };

  const loadAudit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!auditProfileId) return;
    setLoading(true); setError(null);
    try {
      setAuditItems(await getAuditTrail(auditProfileId, auditType || undefined));
    } catch (e: unknown) { setError(e instanceof Error ? e.message : 'Failed'); }
    finally { setLoading(false); }
  };

  const sevColor: Record<string, string> = { LOW: '#22c55e', MEDIUM: '#f59e0b', HIGH: '#ef4444' };

  return (
    <div className="page">
      <h1>Human Review &amp; Audit</h1>

      <div className="tab-bar">
        <button className={tab === 'pending' ? 'tab active' : 'tab'} onClick={() => setTab('pending')}>
          Pending Review
        </button>
        <button className={tab === 'audit' ? 'tab active' : 'tab'} onClick={() => setTab('audit')}>
          Audit Trail
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      {tab === 'pending' && (
        <div>
          <button onClick={loadPending} disabled={loading} className="btn-primary" style={{ marginBottom: 16 }}>
            {loading ? 'Loading…' : 'Load All Pending Items'}
          </button>
          {pendingItems !== null && (
            pendingItems.length === 0
              ? <p className="success">✓ No items currently require human review.</p>
              : (
                <div className="card">
                  <table className="results-table">
                    <thead>
                      <tr><th>Profile</th><th>Type</th><th>Field</th><th>Description</th><th>Severity</th><th>Created</th></tr>
                    </thead>
                    <tbody>
                      {pendingItems.map((item, i) => (
                        <tr key={i}>
                          <td><code>{item.profileId}</code></td>
                          <td>{item.itemType}</td>
                          <td>{item.field}</td>
                          <td>{item.description}</td>
                          <td style={{ color: sevColor[item.severity], fontWeight: 600 }}>{item.severity}</td>
                          <td>{new Date(item.createdAt).toLocaleString('en-CA')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )
          )}
        </div>
      )}

      {tab === 'audit' && (
        <div>
          <form onSubmit={loadAudit} className="card form-card">
            <div className="field-row">
              <div className="field">
                <label htmlFor="audit-profile-id">Profile ID</label>
                <input id="audit-profile-id" value={auditProfileId} onChange={e => setAuditProfileId(e.target.value)} placeholder="Required" />
              </div>
              <div className="field">
                <label htmlFor="audit-type">Filter by Decision Type (optional)</label>
                <select id="audit-type" value={auditType} onChange={e => setAuditType(e.target.value as DecisionType | '')}>
                  <option value="">All types</option>
                  {DECISION_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
            </div>
            <button type="submit" disabled={loading || !auditProfileId} className="btn-primary">
              {loading ? 'Loading…' : 'Load Audit Trail'}
            </button>
          </form>

          {auditItems !== null && (
            auditItems.length === 0
              ? <p className="success">No audit entries found for this profile / filter.</p>
              : (
                <div className="card">
                  <table className="results-table">
                    <thead>
                      <tr><th>#</th><th>Type</th><th>Agent</th><th>Prompt Summary</th><th>AI Output</th><th>Confidence</th><th>Decided At</th></tr>
                    </thead>
                    <tbody>
                      {auditItems.map((a, i) => (
                        <tr key={i}>
                          <td>{a.id}</td>
                          <td>{a.decisionType}</td>
                          <td>{a.agentName}</td>
                          <td className="explanation">{a.promptSummary}</td>
                          <td className="explanation">{a.aiOutput}</td>
                          <td>{a.confidenceScore !== null ? `${Math.round(a.confidenceScore * 100)}%` : '—'}</td>
                          <td>{new Date(a.decidedAt).toLocaleString('en-CA')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )
          )}
        </div>
      )}
    </div>
  );
}
