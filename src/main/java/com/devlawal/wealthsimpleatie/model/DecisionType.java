package com.devlawal.wealthsimpleatie.model;

/**
 * Classifies the type of AI decision captured in {@link AiDecisionLog}.
 */
public enum DecisionType {
    /** Structured extraction of fields from a PDF tax slip. */
    EXTRACTION,
    /** Natural language explanation of a reconciliation discrepancy. */
    RECONCILIATION,
    /** RRSP or investment strategy tradeoff explanation. */
    OPTIMIZATION,
    /** Provincial credit eligibility reasoning and explanation. */
    PROVINCIAL
}
