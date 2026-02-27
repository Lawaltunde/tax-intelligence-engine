package com.devlawal.wealthsimpleatie.util;

import com.devlawal.wealthsimpleatie.model.Province;

/**
 * Deterministic provincial tax rate calculator for the 2024 tax year.
 *
 * Each province uses a progressive bracket structure identical in concept to
 * the federal calculator. Rates and brackets are sourced from CRA's provincial
 * tax schedules and provincial budget announcements.
 *
 * This class intentionally contains NO AI logic — all computations are
 * reproducible, auditable, and testable.
 *
 * Sources:
 *  - https://www.canada.ca/en/revenue-agency/services/tax/individuals/frequently-asked-questions-individuals/canadian-income-tax-rates-individuals-current-previous-years.html
 *  - Provincial budget documents (2024)
 */
public final class ProvincialTaxCalculator {

    private ProvincialTaxCalculator() {}

    /**
     * Computes the provincial tax payable for the given taxable income and province.
     * The basic personal amount credit is NOT deducted here — that is applied separately.
     *
     * @param taxableIncome  net taxable income in CAD
     * @param province       taxpayer's province of residence
     * @return provincial tax payable before credits (CAD)
     */
    public static double calculateProvincialTax(double taxableIncome, Province province) {
        if (taxableIncome <= 0) return 0.0;
        return switch (province) {
            case ON -> calculateOntario(taxableIncome);
            case BC -> calculateBritishColumbia(taxableIncome);
            case AB -> calculateAlberta(taxableIncome);
            case QC -> calculateQuebec(taxableIncome);
            case MB -> calculateManitoba(taxableIncome);
            case SK -> calculateSaskatchewan(taxableIncome);
            case NS -> calculateNovaScotia(taxableIncome);
            case NB -> calculateNewBrunswick(taxableIncome);
            case NL -> calculateNewfoundland(taxableIncome);
            case PE -> calculatePEI(taxableIncome);
            case NT -> calculateNorthwestTerritories(taxableIncome);
            case NU -> calculateNunavut(taxableIncome);
            case YT -> calculateYukon(taxableIncome);
        };
    }

    /**
     * Returns the provincial basic personal amount (non-refundable credit base)
     * for the given province — reduces provincial tax payable.
     */
    public static double basicPersonalAmount(Province province) {
        return switch (province) {
            case ON -> 11_865.0;
            case BC -> 11_981.0;
            case AB -> 21_003.0;
            case QC ->  17_183.0;
            case MB -> 15_780.0;
            case SK -> 17_661.0;
            case NS -> 8_481.0;
            case NB -> 12_458.0;
            case NL -> 10_818.0;
            case PE -> 12_000.0;
            case NT -> 16_593.0;
            case NU -> 17_925.0;
            case YT -> 15_705.0;
        };
    }

    /**
     * Returns the lowest (first) marginal rate for the given province.
     * Used to compute the basic personal amount non-refundable credit correctly:
     *   credit = BPA × lowestRate
     * (Applying the full progressive bracket function to BPA overstates the credit.)
     */
    public static double lowestBracketRate(Province province) {
        return switch (province) {
            case ON -> 0.0505;
            case BC -> 0.0506;
            case AB -> 0.10;
            case QC -> 0.14;
            case MB -> 0.108;
            case SK -> 0.105;
            case NS -> 0.0879;
            case NB -> 0.094;
            case NL -> 0.087;
            case PE -> 0.096;
            case NT -> 0.059;
            case NU -> 0.04;
            case YT -> 0.064;
        };
    }

    // ── Provincial calculators ────────────────────────────────────────────────

    // Ontario — 2024 brackets
    private static double calculateOntario(double income) {
        return bracket(income,
                new double[]{51_446, 102_894, 150_000, 220_000},
                new double[]{0.0505, 0.0915, 0.1116, 0.1216, 0.1316});
    }

    // British Columbia — 2024 brackets
    private static double calculateBritishColumbia(double income) {
        return bracket(income,
                new double[]{45_654, 91_310, 104_835, 127_299, 172_602, 240_716},
                new double[]{0.0506, 0.077, 0.105, 0.1229, 0.147, 0.168, 0.205});
    }

    // Alberta — flat 10% up to $148,269, then surtax tiers (2024)
    private static double calculateAlberta(double income) {
        return bracket(income,
                new double[]{148_269, 177_922, 237_230, 355_845},
                new double[]{0.10, 0.12, 0.13, 0.14, 0.15});
    }

    // Quebec — 2024 brackets
    // Bracket 1: $0 – $51,780          → 14.00%
    // Bracket 2: $51,780 – $103,545    → 19.00%
    // Bracket 3: $103,545 – $126,000   → 24.25%  (reduced to 24% in 2025)
    // Bracket 4: over $126,000         → 25.75%
    private static double calculateQuebec(double income) {
        return bracket(income,
                new double[]{51_780, 103_545, 126_000},
                new double[]{0.14, 0.19, 0.2425, 0.2575});
    }

    // Manitoba — 2024 brackets
    private static double calculateManitoba(double income) {
        return bracket(income,
                new double[]{36_842, 79_625},
                new double[]{0.108, 0.1275, 0.174});
    }

    // Saskatchewan — 2024 brackets
    private static double calculateSaskatchewan(double income) {
        return bracket(income,
                new double[]{49_720, 142_058},
                new double[]{0.105, 0.125, 0.145});
    }

    // Nova Scotia — 2024 brackets
    private static double calculateNovaScotia(double income) {
        return bracket(income,
                new double[]{29_590, 59_180, 93_000, 150_000},
                new double[]{0.0879, 0.1495, 0.1667, 0.175, 0.21});
    }

    // New Brunswick — 2024 brackets
    private static double calculateNewBrunswick(double income) {
        return bracket(income,
                new double[]{47_715, 95_431, 176_756},
                new double[]{0.094, 0.14, 0.16, 0.195});
    }

    // Newfoundland and Labrador — 2024 brackets
    private static double calculateNewfoundland(double income) {
        return bracket(income,
                new double[]{43_198, 86_395, 154_244, 215_943, 275_870},
                new double[]{0.087, 0.145, 0.158, 0.178, 0.198, 0.208});
    }

    // Prince Edward Island — 2024 brackets
    private static double calculatePEI(double income) {
        return bracket(income,
                new double[]{32_656, 64_313},
                new double[]{0.096, 0.1337, 0.1650});
    }

    // Northwest Territories — 2024 brackets
    private static double calculateNorthwestTerritories(double income) {
        return bracket(income,
                new double[]{50_597, 101_198, 164_525},
                new double[]{0.059, 0.086, 0.122, 0.1405});
    }

    // Nunavut — 2024 brackets
    private static double calculateNunavut(double income) {
        return bracket(income,
                new double[]{53_268, 106_537, 173_205},
                new double[]{0.04, 0.07, 0.09, 0.115});
    }

    // Yukon — 2024 brackets
    private static double calculateYukon(double income) {
        return bracket(income,
                new double[]{55_867, 111_733, 154_906, 500_000},
                new double[]{0.064, 0.09, 0.109, 0.128, 0.15});
    }

    // ── Generic progressive bracket engine ───────────────────────────────────

    /**
     * Applies progressive brackets to taxableIncome.
     *
     * @param income    taxable income
     * @param limits    bracket upper limits (length N); income above last limit uses rates[N]
     * @param rates     marginal rates per bracket (length = limits.length + 1)
     */
    static double bracket(double income, double[] limits, double[] rates) {
        double tax = 0.0;
        double prev = 0.0;
        for (int i = 0; i < limits.length; i++) {
            if (income <= limits[i]) {
                tax += (income - prev) * rates[i];
                return round2(tax);
            }
            tax += (limits[i] - prev) * rates[i];
            prev = limits[i];
        }
        tax += (income - prev) * rates[rates.length - 1];
        return round2(tax);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
