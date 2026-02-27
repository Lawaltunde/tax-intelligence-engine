package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.ReconciliationExplanationAgent;
import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.dto.CraImportDto;
import com.devlawal.wealthsimpleatie.model.AiDecisionLog;
import com.devlawal.wealthsimpleatie.model.DecisionType;
import com.devlawal.wealthsimpleatie.model.DiscrepancySeverity;
import com.devlawal.wealthsimpleatie.model.ReconciliationResult;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.ReconciliationResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Core reconciliation engine.
 *
 * Deterministic logic:
 *  - Numeric comparison against configurable mismatch threshold
 *  - Severity classification by percentage delta
 *  - Human escalation flagging
 *
 * LLM boundary:
 *  - Natural language explanation of each discrepancy (delegated to ReconciliationExplanationAgent)
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final ReconciliationExplanationAgent explanationAgent;
    private final AtieProperties properties;
    private final ReconciliationResultRepository reconciliationResultRepository;
    private final AuditLogService auditLogService;

    public ReconciliationService(ReconciliationExplanationAgent explanationAgent,
                                 AtieProperties properties,
                                 ReconciliationResultRepository reconciliationResultRepository,
                                 AuditLogService auditLogService) {
        this.explanationAgent               = explanationAgent;
        this.properties                     = properties;
        this.reconciliationResultRepository = reconciliationResultRepository;
        this.auditLogService                = auditLogService;
    }

    /**
     * Reconciles all fields between the uploaded TaxProfile and CRA-reported data.
     * Results are persisted to the database and returned to the caller.
     */
    @Transactional
    public List<ReconciliationResult> reconcile(TaxProfile uploaded, CraImportDto cra) {
        double threshold = properties.reconciliation().mismatchThreshold();
        List<ReconciliationResult> results = new ArrayList<>();

        reconcileField(uploaded.profileId(), "employmentIncome", uploaded.employmentIncome(), cra.employmentIncome(), threshold)
                .ifPresent(results::add);
        reconcileField(uploaded.profileId(), "rrspContribution", uploaded.rrspContribution(), cra.rrspContribution(), threshold)
                .ifPresent(results::add);
        reconcileField(uploaded.profileId(), "capitalGains",     uploaded.capitalGains(),     cra.capitalGains(),     threshold)
                .ifPresent(results::add);

        // Persist all discrepancies
        reconciliationResultRepository.saveAll(results);

        log.info("Reconciliation complete: {} discrepancies found and persisted", results.size());
        return results;
    }

    // -----------------------------------------------------------------------

    private java.util.Optional<ReconciliationResult> reconcileField(
            String profileId, String field, double uploaded, double cra, double threshold) {

        double delta = Math.abs(uploaded - cra);
        if (delta <= threshold) {
            log.debug("Field '{}' within threshold (delta={})", field, delta);
            return java.util.Optional.empty();
        }

        DiscrepancySeverity severity = classifySeverity(uploaded, cra);
        boolean requiresReview = severity == DiscrepancySeverity.HIGH
                || severity == DiscrepancySeverity.MEDIUM;

        log.info("Discrepancy detected in '{}': uploaded={}, cra={}, delta={}, severity={}",
                field, uploaded, cra, delta, severity);

        // LLM generates explanation only — numeric logic is all above
        String explanation = explanationAgent.explain(field, uploaded, cra, delta);

        // Persist audit trail for every AI explanation
        auditLogService.record(AiDecisionLog.builder()
                .profileId(profileId)
                .decisionType(DecisionType.RECONCILIATION)
                .agentName("ReconciliationExplanationAgent")
                .promptSummary("field=%s uploaded=%.2f cra=%.2f delta=%.2f".formatted(field, uploaded, cra, delta))
                .aiOutput(explanation)
                .build());

        return java.util.Optional.of(
                ReconciliationResult.builder()
                        .profileId(profileId)
                        .field(field)
                        .uploadedValue(uploaded)
                        .craValue(cra)
                        .delta(delta)
                        .severity(severity)
                        .requiresReview(requiresReview)
                        .aiExplanation(explanation)
                        .build()
        );
    }

    /**
     * Classifies severity by percentage discrepancy:
     *   < 5%  → LOW
     *   5–15% → MEDIUM
     *   > 15% → HIGH
     */
    private DiscrepancySeverity classifySeverity(double uploaded, double cra) {
        if (cra == 0) return DiscrepancySeverity.HIGH;
        double pct = Math.abs(uploaded - cra) / cra;
        if (pct < 0.05)  return DiscrepancySeverity.LOW;
        if (pct < 0.15)  return DiscrepancySeverity.MEDIUM;
        return DiscrepancySeverity.HIGH;
    }
}
