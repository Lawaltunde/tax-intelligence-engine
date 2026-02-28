import api from './client';
import type {
  CraAuthRequest,
  CraImportDto,
  ExtractedProfileDto,
  ReconciliationRequestDto,
  ReconciliationResponseDto,
  TaxProfileBody,
  OptimizationResponseDto,
  RiskAssessmentRequestDto,
  RiskResponseDto,
  ProvincialPolicyRequest,
  ProvincialPolicyResponse,
  ReviewItemDto,
  AuditLogDto,
  DecisionType,
} from './types';

// ── CRA Import ────────────────────────────────────────────────────────────────

export const importFromCra = (body: CraAuthRequest) =>
  api.post<CraImportDto>('/api/v1/cra/import', body).then(r => r.data);

// ── Document Extraction ───────────────────────────────────────────────────────

export const extractDocument = (file: File, taxYear: string) => {
  const form = new FormData();
  form.append('file', file);
  form.append('taxYear', taxYear);
  return api
    .post<ExtractedProfileDto>('/api/v1/documents/extract', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then(r => r.data);
};

// ── Reconciliation ────────────────────────────────────────────────────────────

export const runReconciliation = (body: ReconciliationRequestDto) =>
  api.post<ReconciliationResponseDto[]>('/api/v1/reconciliation/run', body).then(r => r.data);

// ── Optimization ──────────────────────────────────────────────────────────────

export const simulateRrsp = (profile: TaxProfileBody, additionalContrib: number) =>
  api
    .post<OptimizationResponseDto>('/api/v1/optimization/rrsp', profile, {
      params: { additionalContrib },
    })
    .then(r => r.data);

// ── Risk Assessment ───────────────────────────────────────────────────────────

export const assessRisk = (body: RiskAssessmentRequestDto) =>
  api.post<RiskResponseDto>('/api/v1/risk/assess', body).then(r => r.data);

// ── Provincial Policy ─────────────────────────────────────────────────────────

export const analyseProvincialPolicy = (body: ProvincialPolicyRequest) =>
  api.post<ProvincialPolicyResponse>('/api/v1/provincial/analyse', body).then(r => r.data);

// ── Human Review ──────────────────────────────────────────────────────────────

export const getAllPendingReview = () =>
  api.get<ReviewItemDto[]>('/api/v1/review/pending').then(r => r.data);

export const getPendingReviewForProfile = (profileId: string) =>
  api.get<ReviewItemDto[]>(`/api/v1/review/pending/${profileId}`).then(r => r.data);

// ── Audit Log ─────────────────────────────────────────────────────────────────

export const getAuditTrail = (profileId: string, type?: DecisionType) =>
  api
    .get<AuditLogDto[]>(`/api/v1/review/audit/${profileId}`, {
      params: type ? { type } : undefined,
    })
    .then(r => r.data);
