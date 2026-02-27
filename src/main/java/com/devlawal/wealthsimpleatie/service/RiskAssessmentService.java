package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.model.RiskAssessment;
import com.devlawal.wealthsimpleatie.model.RiskLevel;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.RiskAssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic pre-filing risk assessment engine.
 *
 * Risk score model (0–100):
 *   Income mismatch detected              → +weights.incomeWeight    (default +20)
 *   Missing slip detected                 → +weights.missingSlip     (default +15)
 *   Large RRSP increase vs room           → +weights.deductionSpike  (default +25)
 *   Income spike > 50% vs prior year      → +weights.incomeWeight    (default +20, capped)
 *
 * Risk levels:
 *   0–30  → LOW
 *   31–60 → MEDIUM
 *   61+   → HIGH
 *
 * No LLM involvement — this is purely deterministic heuristic scoring.
 */
@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private static final int MAX_SCORE      = 100;
    private static final int MEDIUM_FLOOR   = 31;
    private static final int HIGH_FLOOR     = 61;
    /** Income is considered a spike if it grew more than 50% vs prior year. */
    private static final double INCOME_SPIKE_THRESHOLD = 0.50;

    private final AtieProperties properties;
    private final RiskAssessmentRepository riskAssessmentRepository;

    public RiskAssessmentService(AtieProperties properties,
                                 RiskAssessmentRepository riskAssessmentRepository) {
        this.properties               = properties;
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    /**
     * Evaluates the pre-filing risk profile and persists the result.
     */
    public RiskAssessment assess(TaxProfile profile, boolean hasDiscrepancy) {
        AtieProperties.Risk risk = properties.risk();
        List<String> drivers = new ArrayList<>();
        int score = 0;

        if (hasDiscrepancy) {
            score += risk.incomeWeight();
            drivers.add("Employment income mismatch between uploaded slip and CRA data");
        }

        if (profile.employmentIncome() == 0.0) {
            score += risk.missingSlipWeight();
            drivers.add("No employment income detected — slip may be missing");
        }

        if (profile.employmentIncome() > 0
                && profile.rrspContribution() > profile.employmentIncome() * 0.20) {
            score += risk.deductionSpikeWeight();
            drivers.add("RRSP contribution is unusually large relative to employment income");
        }

        // Prior-year income spike detection (requires priorYearIncome > 0 on the profile)
        if (profile.priorYearIncome() > 0) {
            double growthRate = (profile.employmentIncome() - profile.priorYearIncome())
                                / profile.priorYearIncome();
            if (growthRate > INCOME_SPIKE_THRESHOLD) {
                score += risk.incomeWeight();
                drivers.add("Income increased by %.0f%% vs prior year — may trigger CRA review"
                        .formatted(growthRate * 100));
            }
        }

        int clampedScore = Math.min(score, MAX_SCORE);
        RiskLevel level = classifyLevel(clampedScore);

        log.info("Risk assessment: score={}, level={}, drivers={}", clampedScore, level, drivers);

        RiskAssessment assessment = RiskAssessment.builder()
                .profileId(profile.profileId())
                .riskScore(clampedScore)
                .riskLevel(level)
                .drivers(drivers)
                .build();

        // Persist to database
        riskAssessmentRepository.save(assessment);

        return assessment;
    }

    // -----------------------------------------------------------------------

    private RiskLevel classifyLevel(int score) {
        if (score >= HIGH_FLOOR)   return RiskLevel.HIGH;
        if (score >= MEDIUM_FLOOR) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}
