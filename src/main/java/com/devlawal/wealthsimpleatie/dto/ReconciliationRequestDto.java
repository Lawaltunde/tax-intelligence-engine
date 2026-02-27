package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.ConfidenceScores;

/**
 * Request body for the reconciliation endpoint.
 * Bundles the taxpayer's uploaded tax data with the SIN needed to pull CRA data —
 * keeping sensitive identifiers in the body (not the URL).
 *
 * The frontend should populate this from the ExtractedProfileDto returned by
 * POST /api/v1/documents/extract, storing profileId and reusing the income fields.
 */
public record ReconciliationRequestDto(
        String profileId,
        String sin,
        String taxYear,
        double employmentIncome,
        double rrspContribution,
        double capitalGains,
        double rrspContributionRoom,
        double capitalLossCarryforward,
        double tuitionCarryforward,
        double priorYearIncome,
        ConfidenceScores confidenceScores
) {}
