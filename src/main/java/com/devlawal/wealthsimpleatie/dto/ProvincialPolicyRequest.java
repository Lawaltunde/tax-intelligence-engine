package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.Province;

/**
 * Request body for the provincial policy reasoning endpoint.
 *
 * @param profileId         the TaxProfile this analysis is for
 * @param province          the taxpayer's province of residence
 * @param employmentIncome  employment income for the tax year
 * @param rrspContribution  RRSP contribution for the year
 * @param capitalGains      net capital gains for the year
 * @param tuitionAmount     eligible tuition fees (T2202) — 0 if not applicable
 * @param hasDependents     true if the taxpayer has eligible dependents
 * @param isStudent         true if the taxpayer is a full/part-time student
 * @param taxYear           tax year (e.g. "2024")
 */
public record ProvincialPolicyRequest(
        String profileId,
        Province province,
        double employmentIncome,
        double rrspContribution,
        double capitalGains,
        double tuitionAmount,
        boolean hasDependents,
        boolean isStudent,
        String taxYear
) {}
