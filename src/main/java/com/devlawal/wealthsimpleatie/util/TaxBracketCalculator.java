package com.devlawal.wealthsimpleatie.util;

/**
 * Deterministic 2025 Canadian federal marginal tax bracket calculator.
 *
 * Bracket thresholds (2025 federal):
 *   $0       – $57,375   →  15%
 *   $57,375  – $114,750  →  20.5%
 *   $114,750 – $158,519  →  26%
 *   $158,519 – $220,000  →  29%
 *   $220,000+            →  33%
 *
 * Provincial tax is excluded from this MVP model.
 * All tax amounts are in Canadian dollars.
 */
public final class TaxBracketCalculator {

    // Federal bracket thresholds (2025)
    private static final double BRACKET_1_TOP  = 57_375.00;
    private static final double BRACKET_2_TOP  = 114_750.00;
    private static final double BRACKET_3_TOP  = 158_519.00;
    private static final double BRACKET_4_TOP  = 220_000.00;

    // Federal marginal rates
    private static final double RATE_1 = 0.15;
    private static final double RATE_2 = 0.205;
    private static final double RATE_3 = 0.26;
    private static final double RATE_4 = 0.29;
    private static final double RATE_5 = 0.33;

    /**
     * 2025 federal Basic Personal Amount.
     * Every Canadian resident gets a non-refundable credit equal to BPA × 15%.
     * This is applied inside calculateFederalTax to arrive at net federal tax.
     * Source: CRA T1 General 2025, Schedule 1 line 30000.
     */
    public static final double FEDERAL_BASIC_PERSONAL_AMOUNT = 16_129.00;

    /**
     * Non-refundable BPA credit ($16,129 × 15% = $2,419.35).
     * Pre-computed here so it is used consistently.
     */
    public static final double FEDERAL_BPA_CREDIT =
            round(FEDERAL_BASIC_PERSONAL_AMOUNT * RATE_1);

    /**
     * The bottom federal bracket rate (15%).
     * Used externally to compute non-refundable credits (e.g. tuition carryforward)
     * which are always valued at the lowest marginal rate per CRA Schedule 11.
     */
    public static final double RATE_BOTTOM = RATE_1;

    /** Utility class — no instantiation. */
    private TaxBracketCalculator() {}

    /**
     * Computes federal income tax payable on the given taxable income.
     * Includes the Basic Personal Amount non-refundable credit (2025: $16,129 × 15% = $2,419.35).
     *
     * @param taxableIncome gross employment income minus RRSP deductions (plus 50% of net capital gains)
     * @return net federal tax payable after BPA credit (always ≥ 0)
     */
    public static double calculateFederalTax(double taxableIncome) {
        if (taxableIncome <= 0) return 0.0;

        double tax = 0.0;
        double remaining = taxableIncome;

        // Bracket 1
        double bracket1 = Math.min(remaining, BRACKET_1_TOP);
        tax += bracket1 * RATE_1;
        remaining -= bracket1;
        if (remaining <= 0) return Math.max(0, round(tax) - FEDERAL_BPA_CREDIT);

        // Bracket 2
        double bracket2 = Math.min(remaining, BRACKET_2_TOP - BRACKET_1_TOP);
        tax += bracket2 * RATE_2;
        remaining -= bracket2;
        if (remaining <= 0) return Math.max(0, round(tax) - FEDERAL_BPA_CREDIT);

        // Bracket 3
        double bracket3 = Math.min(remaining, BRACKET_3_TOP - BRACKET_2_TOP);
        tax += bracket3 * RATE_3;
        remaining -= bracket3;
        if (remaining <= 0) return Math.max(0, round(tax) - FEDERAL_BPA_CREDIT);

        // Bracket 4
        double bracket4 = Math.min(remaining, BRACKET_4_TOP - BRACKET_3_TOP);
        tax += bracket4 * RATE_4;
        remaining -= bracket4;
        if (remaining <= 0) return Math.max(0, round(tax) - FEDERAL_BPA_CREDIT);

        // Bracket 5
        tax += remaining * RATE_5;
        return Math.max(0, round(tax) - FEDERAL_BPA_CREDIT);
    }

    /**
     * Returns the marginal rate that applies to the last dollar of income.
     *
     * @param taxableIncome total taxable income
     * @return marginal rate as a fraction (e.g. 0.205)
     */
    public static double marginalRate(double taxableIncome) {
        if (taxableIncome <= 0)              return 0.0;
        if (taxableIncome <= BRACKET_1_TOP)  return RATE_1;
        if (taxableIncome <= BRACKET_2_TOP)  return RATE_2;
        if (taxableIncome <= BRACKET_3_TOP)  return RATE_3;
        if (taxableIncome <= BRACKET_4_TOP)  return RATE_4;
        return RATE_5;
    }

    private static double round(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
