// ── Enums (mirroring Java model) ─────────────────────────────────────────────

export type Province =
  | 'AB' | 'BC' | 'MB' | 'NB' | 'NL' | 'NS'
  | 'NT' | 'NU' | 'ON' | 'PE' | 'QC' | 'SK' | 'YT';

export type DiscrepancySeverity = 'LOW' | 'MEDIUM' | 'HIGH';
export type RiskLevel           = 'LOW' | 'MEDIUM' | 'HIGH';
export type DecisionType        = 'RECONCILIATION' | 'OPTIMIZATION' | 'RISK_ASSESSMENT' | 'PROVINCIAL_POLICY' | 'EXTRACTION';

// ── Shared sub-types ──────────────────────────────────────────────────────────

export interface ConfidenceScores {
  employmentIncome:       number;
  rrspContribution:       number;
  capitalGains:           number;
  rrspContributionRoom:   number;
  capitalLossCarryforward:number;
  tuitionCarryforward:    number;
}

// ── CRA Import ────────────────────────────────────────────────────────────────

export interface CraAuthRequest {
  sin:               string;
  dateOfBirth:       string;   // yyyy-MM-dd
  netfileAccessCode: string;
  taxYear:           string;
}

export interface CraImportDto {
  employmentIncome:   number;
  employerName:       string;
  incomeTaxDeducted:  number;
  rrspContribution:   number;
  rrspDeductionLimit: number;
  capitalGains:       number;
  dividendIncome:     number;
  otherIncome:        number;
  taxYear:            string;
  sinLast3:           string;
  dataSource:         'CRA_AFR' | 'MOCK';
}

// ── Document Extraction ───────────────────────────────────────────────────────

export interface ExtractedProfileDto {
  profileId:               string;
  taxYear:                 string;
  employmentIncome:        number;
  rrspContribution:        number;
  capitalGains:            number;
  rrspContributionRoom:    number;
  capitalLossCarryforward: number;
  tuitionCarryforward:     number;
  confidenceScores:        ConfidenceScores;
}

// ── Reconciliation ────────────────────────────────────────────────────────────

export interface ReconciliationRequestDto {
  profileId:               string;
  sin:                     string;
  taxYear:                 string;
  employmentIncome:        number;
  rrspContribution:        number;
  capitalGains:            number;
  rrspContributionRoom:    number;
  capitalLossCarryforward: number;
  tuitionCarryforward:     number;
  priorYearIncome:         number;
  confidenceScores?:       ConfidenceScores;
}

export interface ReconciliationResponseDto {
  field:          string;
  uploadedValue:  number;
  craValue:       number;
  delta:          number;
  severity:       DiscrepancySeverity;
  requiresReview: boolean;
  aiExplanation:  string;
}

// ── Optimization ──────────────────────────────────────────────────────────────

export interface TaxProfileBody {
  profileId:               string;
  taxYear:                 string;
  employmentIncome:        number;
  rrspContribution:        number;
  capitalGains:            number;
  rrspContributionRoom:    number;
  capitalLossCarryforward: number;
  tuitionCarryforward:     number;
  priorYearIncome:         number;
  confidenceScores?:       ConfidenceScores;
}

export interface OptimizationResponseDto {
  additionalContribution:        number;
  currentTax:                    number;
  optimizedTax:                  number;
  estimatedSavings:              number;
  capitalLossCarryforwardApplied:number;
  tuitionCreditApplied:          number;
  aiTradeoffExplanation:         string;
}

// ── Risk Assessment ───────────────────────────────────────────────────────────

export interface RiskAssessmentRequestDto {
  profileId:               string;
  taxYear:                 string;
  employmentIncome:        number;
  rrspContribution:        number;
  capitalGains:            number;
  rrspContributionRoom:    number;
  capitalLossCarryforward: number;
  tuitionCarryforward:     number;
  priorYearIncome:         number;
  hasDiscrepancy:          boolean;
  confidenceScores?:       ConfidenceScores;
}

export interface RiskResponseDto {
  riskScore: number;
  riskLevel: RiskLevel;
  drivers:   string[];
}

// ── Provincial Policy ─────────────────────────────────────────────────────────

export interface ProvincialPolicyRequest {
  profileId:        string;
  province:         Province;
  employmentIncome: number;
  rrspContribution: number;
  capitalGains:     number;
  tuitionAmount:    number;
  hasDependents:    boolean;
  isStudent:        boolean;
  taxYear:          string;
}

export interface ProvincialPolicyResponse {
  province:              Province;
  provincialTaxPayable:  number;
  estimatedCreditsTotal: number;
  netProvincialTax:      number;
  eligibleCredits:       string[];
  policyExplanation:     string;
  recommendations:       string[];
}

// ── Human Review & Audit ──────────────────────────────────────────────────────

export interface ReviewItemDto {
  profileId:    string;
  itemType:     string;
  field:        string;
  description:  string;
  severity:     DiscrepancySeverity;
  aiExplanation:string;
  createdAt:    string; // ISO instant
}

export interface AuditLogDto {
  id:              number;
  profileId:       string;
  decisionType:    DecisionType;
  agentName:       string;
  promptSummary:   string;
  aiOutput:        string;
  confidenceScore: number | null;
  decidedAt:       string; // ISO instant
}
