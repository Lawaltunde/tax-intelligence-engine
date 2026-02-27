package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.ProvincialPolicyAgent;
import com.devlawal.wealthsimpleatie.dto.ProvincialPolicyRequest;
import com.devlawal.wealthsimpleatie.dto.ProvincialPolicyResponse;
import com.devlawal.wealthsimpleatie.model.AiDecisionLog;
import com.devlawal.wealthsimpleatie.model.DecisionType;
import com.devlawal.wealthsimpleatie.util.ProvincialTaxCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provincial tax policy orchestration service.
 *
 * Architecture (hybrid deterministic + AI):
 *
 *  DETERMINISTIC (this class):
 *   - Provincial tax calculation using ProvincialTaxCalculator (bracket tables)
 *   - Basic personal amount credit application
 *   - Final net provincial tax computation
 *
 *  AI-DELEGATED (ProvincialPolicyAgent):
 *   - Identifying applicable provincial credits
 *   - Explaining eligibility rules in plain language
 *   - Strategic recommendation generation
 *
 *  AUDIT:
 *   - Every AI response is logged to ai_decision_logs via AuditLogService
 */
@Service
public class ProvincialPolicyService {

    private static final Logger log = LoggerFactory.getLogger(ProvincialPolicyService.class);

    private final ProvincialPolicyAgent policyAgent;
    private final AuditLogService auditLogService;

    public ProvincialPolicyService(ProvincialPolicyAgent policyAgent,
                                   AuditLogService auditLogService) {
        this.policyAgent     = policyAgent;
        this.auditLogService = auditLogService;
    }

    /**
     * Analyses provincial tax obligations and credit eligibility for the taxpayer.
     *
     * @param request  taxpayer profile + province + tax year
     * @return full provincial policy response with deterministic tax figures and AI-generated credits
     */
    public ProvincialPolicyResponse analyse(ProvincialPolicyRequest request) {
        // ── Step 1: Deterministic provincial tax calculation ─────────────────
        double taxableIncome = Math.max(0,
                request.employmentIncome() - request.rrspContribution());

        double grossProvincialTax = ProvincialTaxCalculator
                .calculateProvincialTax(taxableIncome, request.province());

        // Apply basic personal amount credit (bottom-bracket rate × BPA)
        double bpa = ProvincialTaxCalculator.basicPersonalAmount(request.province());
        double bpaCredit = ProvincialTaxCalculator
                .calculateProvincialTax(bpa, request.province());
        double provincialTaxBeforeOtherCredits = Math.max(0, grossProvincialTax - bpaCredit);

        log.info("Provincial tax: province={}, taxableIncome={}, grossTax={}, bpaCredit={}, beforeOtherCredits={}",
                request.province(), taxableIncome, grossProvincialTax, bpaCredit,
                provincialTaxBeforeOtherCredits);

        // ── Step 2: AI identifies additional credits and generates explanation ─
        ProvincialPolicyAgent.ProvincialPolicyResponse aiResponse = policyAgent.analyse(
                request.province(),
                request.employmentIncome(),
                request.rrspContribution(),
                request.capitalGains(),
                request.tuitionAmount(),
                request.hasDependents(),
                request.isStudent(),
                provincialTaxBeforeOtherCredits,
                request.taxYear());

        // ── Step 3: Apply AI-identified credits to compute net provincial tax ─
        double totalCredits = bpaCredit + aiResponse.estimatedCreditsTotal();
        double netProvincialTax = Math.max(0, grossProvincialTax - totalCredits);

        log.info("Provincial analysis complete: province={}, netTax={}, totalCredits={}",
                request.province(), netProvincialTax, totalCredits);

        // ── Step 4: Audit log the AI decision ─────────────────────────────────
        auditLogService.record(AiDecisionLog.builder()
                .profileId(request.profileId())
                .decisionType(DecisionType.PROVINCIAL)
                .agentName("ProvincialPolicyAgent")
                .promptSummary("province=%s income=%.2f rrsp=%.2f tuition=%.2f student=%s dependents=%s"
                        .formatted(request.province(), request.employmentIncome(),
                                request.rrspContribution(), request.tuitionAmount(),
                                request.isStudent(), request.hasDependents()))
                .aiOutput(aiResponse.policyExplanation())
                .build());

        // ── Step 5: Build and return response ─────────────────────────────────
        List<String> allCredits = new java.util.ArrayList<>();
        allCredits.add("Basic Personal Amount credit: $%,.2f".formatted(bpaCredit));
        if (aiResponse.eligibleCredits() != null) {
            allCredits.addAll(aiResponse.eligibleCredits());
        }

        return new ProvincialPolicyResponse(
                request.province(),
                grossProvincialTax,
                totalCredits,
                netProvincialTax,
                allCredits,
                aiResponse.policyExplanation(),
                aiResponse.recommendations() != null ? aiResponse.recommendations() : List.of()
        );
    }
}
