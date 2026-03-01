package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.OptimizationExplanationAgent;
import com.devlawal.wealthsimpleatie.model.AiDecisionLog;
import com.devlawal.wealthsimpleatie.model.DecisionType;
import com.devlawal.wealthsimpleatie.model.OptimizationResult;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.util.TaxBracketCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * RRSP contribution optimization simulator.
 *
 * Deterministic logic:
 *  - All marginal tax bracket computations (2025 federal brackets)
 *  - Capital loss carryforward reduces net capital gains (CRA Schedule 3)
 *  - Tuition carryforward applied as a non-refundable credit (CRA Schedule 11)
 *  - Current / optimized tax payable
 *  - Estimated savings
 *
 * LLM boundary:
 *  - Human-readable tradeoff explanation (delegated to OptimizationExplanationAgent)
 */
@Service
public class OptimizationService {

    private static final Logger log = LoggerFactory.getLogger(OptimizationService.class);

    private final OptimizationExplanationAgent explanationAgent;
    private final AuditLogService auditLogService;

    public OptimizationService(OptimizationExplanationAgent explanationAgent,
                               AuditLogService auditLogService) {
        this.explanationAgent = explanationAgent;
        this.auditLogService  = auditLogService;
    }

    /**
     * Simulates the tax impact of an additional RRSP contribution.
     * Also applies capital loss carryforward and tuition carryforward from the profile.
     *
     * @param profile              taxpayer's current tax profile
     * @param additionalContrib    proposed additional RRSP contribution in CAD
     * @return full optimization result including deterministic savings and AI explanation
     */
    public OptimizationResult simulateRrspContribution(TaxProfile profile,
                                                       double additionalContrib) {
        if (additionalContrib < 0) {
            throw new IllegalArgumentException(
                    "additionalContrib must be >= 0, got: " + additionalContrib);
        }
        double baseIncome    = profile.employmentIncome();
        double existingRrsp  = profile.rrspContribution();

        // ── Enforce RRSP deduction room (CRA rule: total deduction ≤ deduction limit) ──
        // rrspContributionRoom on the profile is the limit from the taxpayer's prior-year NOA.
        // The simulation must not propose a deduction that exceeds remaining room, otherwise
        // the calculated savings would be for a contribution CRA will not allow.
        double remainingRoom = Math.max(0, profile.rrspContributionRoom() - existingRrsp);
        double effectiveAdditionalContrib = Math.min(additionalContrib, remainingRoom);
        if (effectiveAdditionalContrib < additionalContrib) {
            log.warn("Requested additionalContrib={} exceeds remaining RRSP room={}; " +
                     "capping simulation at {}",
                    additionalContrib, remainingRoom, effectiveAdditionalContrib);
        }

        // ── Capital loss carryforward reduces net capital gains (CRA Schedule 3) ─
        double capitalLossApplied = Math.min(profile.capitalLossCarryforward(), profile.capitalGains());
        double netCapitalGains = Math.max(0,
                profile.capitalGains() - profile.capitalLossCarryforward());
        // Only 50% of net capital gains are taxable (inclusion rate)
        double taxableCapitalGains = netCapitalGains * 0.50;

        // ── Deterministic tax computation ─────────────────────────────────────
        double currentTaxableIncome   = Math.max(0,
                baseIncome - existingRrsp + taxableCapitalGains);
        double optimizedTaxableIncome = Math.max(0,
                baseIncome - existingRrsp - effectiveAdditionalContrib + taxableCapitalGains);

        double currentTaxBeforeCredits   = TaxBracketCalculator.calculateFederalTax(currentTaxableIncome);
        double optimizedTaxBeforeCredits = TaxBracketCalculator.calculateFederalTax(optimizedTaxableIncome);

        // ── Tuition carryforward: non-refundable credit at lowest federal rate ─
        // CRA applies unused tuition credits at the 15% federal rate (bottom bracket)
        double tuitionCredit = profile.tuitionCarryforward() * TaxBracketCalculator.RATE_BOTTOM;
        double currentTax    = Math.max(0, currentTaxBeforeCredits   - tuitionCredit);
        double optimizedTax  = Math.max(0, optimizedTaxBeforeCredits - tuitionCredit);
        double savings       = Math.max(0, currentTax - optimizedTax);
        double marginalRate  = TaxBracketCalculator.marginalRate(currentTaxableIncome);

        log.info("Optimization: baseIncome={}, requestedContrib={}, effectiveContrib={}, " +
                 "remainingRoom={}, capitalLossCarryforward={}, " +
                 "tuitionCarryforward={}, currentTax={}, optimizedTax={}, savings={}",
                baseIncome, additionalContrib, effectiveAdditionalContrib, remainingRoom,
                profile.capitalLossCarryforward(),
                profile.tuitionCarryforward(), currentTax, optimizedTax, savings);

        // LLM generates explanation only
        String explanation = explanationAgent.explain(
                effectiveAdditionalContrib, currentTax, optimizedTax, savings, marginalRate);

        // Persist audit trail
        auditLogService.record(AiDecisionLog.builder()
                .profileId(profile.profileId())
                .decisionType(DecisionType.OPTIMIZATION)
                .agentName("OptimizationExplanationAgent")
                .promptSummary(("requestedContrib=%.2f effectiveContrib=%.2f remainingRoom=%.2f " +
                                "currentTax=%.2f optimizedTax=%.2f savings=%.2f " +
                                "capitalLossCarryforward=%.2f tuitionCarryforward=%.2f")
                        .formatted(additionalContrib, effectiveAdditionalContrib, remainingRoom,
                                currentTax, optimizedTax, savings,
                                profile.capitalLossCarryforward(), profile.tuitionCarryforward()))
                .aiOutput(explanation)
                .build());

        return OptimizationResult.builder()
                .additionalContribution(effectiveAdditionalContrib)
                .currentTax(currentTax)
                .optimizedTax(optimizedTax)
                .estimatedSavings(savings)
                .capitalLossCarryforwardApplied(capitalLossApplied)
                .tuitionCreditApplied(tuitionCredit)
                .aiTradeoffExplanation(explanation)
                .build();
    }
}
