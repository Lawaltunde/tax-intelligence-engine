package com.devlawal.wealthsimpleatie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data returned by the CRA Auto-fill My Return (AFR) service.
 *
 * Field mapping to CRA T-slip lines:
 *  - employmentIncome    → T4 Box 14  (Employment income)
 *  - employerName        → T4 Box 54  (Employer name)
 *  - incomeTaxDeducted   → T4 Box 22  (Income tax deducted)
 *  - rrspContribution    → T4RSP / RRSP receipts total
 *  - rrspDeductionLimit  → RRSP deduction limit from NOA line 26500
 *  - capitalGains        → T5008 / Schedule 3 net capital gains
 *  - dividendIncome      → T5 Box 25  (Eligible dividends) + Box 10 (Other dividends)
 *  - otherIncome         → T4A Box 28 (Other income)
 *  - taxYear             → The year this data was pulled for
 *  - sinLast3            → Last 3 digits of SIN for UI confirmation (never full SIN)
 *  - dataSource          → "CRA_AFR" when live, "MOCK" during dev/uncertified mode
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CraImportDto(
        double employmentIncome,
        String employerName,
        double incomeTaxDeducted,
        double rrspContribution,
        double rrspDeductionLimit,
        double capitalGains,
        double dividendIncome,
        double otherIncome,
        String taxYear,
        String sinLast3,
        String dataSource
) {
    /** Convenience factory for the fields reconciliation cares about (mock / test use). */
    public static CraImportDto mock(double employmentIncome, double rrspContribution, double capitalGains) {
        return new CraImportDto(
                employmentIncome, "Mock Employer Ltd.", 0,
                rrspContribution, 0,
                capitalGains, 0, 0,
                "2024", "***", "MOCK"
        );
    }
}
