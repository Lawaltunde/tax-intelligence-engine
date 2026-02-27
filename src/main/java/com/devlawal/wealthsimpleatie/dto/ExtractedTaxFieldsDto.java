package com.devlawal.wealthsimpleatie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Structured JSON schema returned by the AI extraction agent.
 * Nulls are permitted because the AI may not find every field in a given document.
 * Confidence map mirrors field names.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractedTaxFieldsDto(
        Double employmentIncome,
        Double rrspContribution,
        Double capitalGains,
        ConfidenceDto confidence
) {
    /** Nested confidence block exactly matching the prompt schema. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfidenceDto(
            Double employmentIncome,
            Double rrspContribution,
            Double capitalGains
    ) {
        public static ConfidenceDto empty() {
            return new ConfidenceDto(0.0, 0.0, 0.0);
        }
    }
}
