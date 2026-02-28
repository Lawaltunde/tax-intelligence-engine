import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { getAllPendingReview, getAuditTrail } from '../api/endpoints';
import type { ReviewItemDto, AuditLogDto, DecisionType } from '../api/types';
import { CardSkeleton } from '../components/Skeleton';

const DECISION_TYPES: DecisionType[] = [
  'RECONCILIATION', 'OPTIMIZATION', 'RISK_ASSESSMENT', 'PROVINCIAL_POLICY', 'EXTRACTION',
];

export default function ReviewPage() {
  const [tab, setTab]                       = useState<'pending' | 'audit'>('pending');
  const [auditProfileId, setAuditProfileId] = useState('');
  const [auditType, setAuditType]           = useState<DecisionType | ''>('');

  // ── Pending Review — auto-fetched whenever the 'pending' tab is active ──────
  const {
    data: pendingItems,
    isFetching: pendingLoading,
    isError: pendingIsError,
    error: pendingError,
    refetch: refetchPending,
  } = useQuery<ReviewItemDto[], Error>({
    queryKey: ['pending-review'],
    queryFn: getAllPendingReview,
    enabled: tab === 'pending',   // only fetch while on this tab
  });

  // ── Audit Trail — triggered on form submit ───────────────────────────────────
  const {
    mutate: loadAudit,
    data: auditItems,
    isPending: auditLoading,
    isError: auditIsError,
    error: auditError,
    reset: resetAudit,
  } = useMutation<AuditLogDto[], Error, { profileId: string; type?: DecisionType }>({
    mutationFn: ({ profileId, type }) => getAuditTrail(profileId, type),
  });

  const handleAuditSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!auditProfileId) return;
    resetAudit();
    loadAudit({ profileId: auditProfileId, type: auditType || undefined });
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

      {tab === 'pending' && (
        <div>
          <button
            onClick={() => refetchPending()}
            disabled={pendingLoading}
            className="btn-primary"
            style={{ marginBottom: 16 }}
          >
            {pendingLoading ? 'Loading…' : 'Refresh'}
          </button>

          {pendingIsError && <p className="error">{pendingError.message}</p>}
          {pendingLoading && <CardSkeleton />}

          {pendingItems !== undefined && !pendingLoading && (
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
          <form onSubmit={handleAuditSubmit} className="card form-card">
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
            <button type="submit" disabled={auditLoading || !auditProfileId} className="btn-primary">
              {auditLoading ? 'Loading…' : 'Load Audit Trail'}
            </button>
            {auditIsError && <p className="error">{auditError.message}</p>}
          </form>

          {auditLoading && <CardSkeleton />}

          {auditItems !== undefined && !auditLoading && (
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
