package com.devlawal.wealthsimpleatie.dto;

/**
 * Credentials required to authenticate with the CRA Auto-fill My Return (AFR) service.
 *
 * All four fields are mandatory for CRA to authorize the data pull:
 *  - sin:             9-digit Social Insurance Number (no dashes)
 *  - dateOfBirth:     taxpayer DOB in ISO format (yyyy-MM-dd)
 *  - netfileAccessCode: 4-character alphanumeric code printed on the taxpayer's
 *                       most recent Notice of Assessment (NOA)
 *  - taxYear:         the tax year to retrieve (e.g. "2024")
 */
public record CraAuthRequest(
        String sin,
        String dateOfBirth,
        String netfileAccessCode,
        String taxYear
) {
    /**
     * Returns a log-safe representation — SIN and NAC are masked.
     */
    public String toMaskedString() {
        String maskedSin = sin != null && sin.length() >= 4
                ? "***-***-" + sin.substring(sin.length() - 3)
                : "***";
        return "CraAuthRequest{sin='%s', dob='%s', nac='****', taxYear='%s'}"
                .formatted(maskedSin, dateOfBirth, taxYear);
    }

    /**
     * Overrides auto-generated toString() to prevent SIN and NAC from leaking into logs.
     * Always delegates to the masked representation.
     */
    @Override
    public String toString() {
        return toMaskedString();
    }
}
