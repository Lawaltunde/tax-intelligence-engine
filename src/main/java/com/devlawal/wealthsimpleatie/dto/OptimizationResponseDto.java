package com.devlawal.wealthsimpleatie.dto;

/**
 * API response DTO for the RRSP optimization simulation.
 * All monetary values are in Canadian dollars.
 *
 * capitalLossCarryforwardApplied — how much of the carry-forward offset capital gains
 * tuitionCreditApplied           — dollar value of the tuition credit applied (carryforward × 15%)
 */
public record OptimizationResponseDto(
        double additionalContribution,
        double currentTax,
        double optimizedTax,
        double estimatedSavings,
        double capitalLossCarryforwardApplied,
        double tuitionCreditApplied,
        String aiTradeoffExplanation
) {}
