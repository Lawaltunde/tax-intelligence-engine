package com.devlawal.wealthsimpleatie.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaxBracketCalculator")
class TaxBracketCalculatorTest {

    @Test
    @DisplayName("zero income produces zero tax")
    void zeroIncome() {
        assertThat(TaxBracketCalculator.calculateFederalTax(0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("negative income produces zero tax")
    void negativeIncome() {
        assertThat(TaxBracketCalculator.calculateFederalTax(-5000)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("income fully within bracket 1 taxed at 15%")
    void bracket1Only() {
        double income = 40_000.00;
        double expected = Math.round(income * 0.15 * 100.0) / 100.0;
        assertThat(TaxBracketCalculator.calculateFederalTax(income)).isEqualTo(expected);
    }

    @Test
    @DisplayName("income spanning bracket 1 and 2 applies correct marginal rates")
    void bracket1And2() {
        double income = 80_000.00;
        double bracket1Tax = 57_375.00 * 0.15;
        double bracket2Tax = (80_000.00 - 57_375.00) * 0.205;
        double expected = Math.round((bracket1Tax + bracket2Tax) * 100.0) / 100.0;
        assertThat(TaxBracketCalculator.calculateFederalTax(income)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "income={0} → marginalRate={1}")
    @CsvSource({
            "0,      0.0",
            "-5000,  0.0",
            "30000,  0.15",
            "57375,  0.15",
            "60000,  0.205",
            "114750, 0.205",
            "120000, 0.26",
            "158519, 0.26",
            "200000, 0.29",
            "250000, 0.33"
    })
    @DisplayName("marginal rate returns correct bracket for income level")
    void marginalRate(double income, double expectedRate) {
        assertThat(TaxBracketCalculator.marginalRate(income)).isEqualTo(expectedRate);
    }

    @Test
    @DisplayName("tax increases monotonically with income")
    void taxIncreasesMonotonically() {
        double previous = 0;
        for (int income = 10_000; income <= 300_000; income += 10_000) {
            double current = TaxBracketCalculator.calculateFederalTax(income);
            assertThat(current).isGreaterThanOrEqualTo(previous);
            previous = current;
        }
    }
}
