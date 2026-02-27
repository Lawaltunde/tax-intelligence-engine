package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.Province;
import java.util.List;

/**
 * Response from the provincial policy reasoning engine.
 *
 * Deterministic fields (computed from rate tables):
 *  - provincialTaxPayable     → calculated provincial tax before credits
 *  - estimatedCreditsTotal    → sum of all eligible credits detected
 *  - netProvincialTax         → provincialTaxPayable minus estimatedCreditsTotal
 *
 * AI-generated fields:
 *  - eligibleCredits          → list of detected applicable credits with descriptions
 *  - policyExplanation        → plain-language explanation of provincial rules applied
 *  - recommendations          → ordered list of strategic recommendations
 */
public record ProvincialPolicyResponse(
        Province province,
        double provincialTaxPayable,
        double estimatedCreditsTotal,
        double netProvincialTax,
        List<String> eligibleCredits,
        String policyExplanation,
        List<String> recommendations
) {}
