package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.DiscrepancySeverity;

/**
 * API response DTO for a reconciled field discrepancy.
 */
public record ReconciliationResponseDto(
        String field,
        double uploadedValue,
        double craValue,
        double delta,
        DiscrepancySeverity severity,
        boolean requiresReview,
        String aiExplanation
) {}
