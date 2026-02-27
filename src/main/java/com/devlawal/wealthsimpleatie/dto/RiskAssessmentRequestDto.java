package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.ConfidenceScores;

/**
 * Request body for the risk assessment endpoint.
 * Consolidates the TaxProfile fields and the reconciliation flag in one body.
 */
public record RiskAssessmentRequestDto(
        String profileId,
        String taxYear,
        double employmentIncome,
        double rrspContribution,
        double capitalGains,
        double rrspContributionRoom,
        double capitalLossCarryforward,
        double tuitionCarryforward,
        double priorYearIncome,
        boolean hasDiscrepancy,
        ConfidenceScores confidenceScores
) {}
