package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.ConfidenceScores;
import com.devlawal.wealthsimpleatie.model.TaxProfile;

/**
 * API response DTO for the document extraction endpoint.
 * Replaces returning the raw TaxProfile JPA entity to the frontend.
 *
 * All monetary values are in Canadian dollars.
 */
public record ExtractedProfileDto(
        String profileId,
        String taxYear,
        double employmentIncome,
        double rrspContribution,
        double capitalGains,
        double rrspContributionRoom,
        double capitalLossCarryforward,
        double tuitionCarryforward,
        ConfidenceScores confidenceScores
) {
    /** Factory — maps from the domain model to this DTO. */
    public static ExtractedProfileDto from(TaxProfile p) {
        return new ExtractedProfileDto(
                p.profileId(),
                p.taxYear(),
                p.employmentIncome(),
                p.rrspContribution(),
                p.capitalGains(),
                p.rrspContributionRoom(),
                p.capitalLossCarryforward(),
                p.tuitionCarryforward(),
                p.confidenceScores()
        );
    }
}
