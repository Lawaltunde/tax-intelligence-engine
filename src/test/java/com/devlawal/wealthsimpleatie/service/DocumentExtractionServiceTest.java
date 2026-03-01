package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.TaxSlipExtractionAgent;
import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.dto.ExtractedTaxFieldsDto;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.TaxProfileRepository;
import com.devlawal.wealthsimpleatie.util.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentExtractionService — multi-slip merge")
class DocumentExtractionServiceTest {

    @Mock private PdfTextExtractor pdfExtractor;
    @Mock private TaxSlipExtractionAgent extractionAgent;
    @Mock private TaxProfileRepository taxProfileRepository;

    private DocumentExtractionService service;

    @BeforeEach
    void setUp() {
        AtieProperties properties = new AtieProperties(
                new AtieProperties.Reconciliation(100.0),
                new AtieProperties.Confidence(0.80),
                new AtieProperties.Risk(Map.of(
                        "income-mismatch", 20, "missing-slip", 15, "deduction-spike", 25))
        );
        service = new DocumentExtractionService(pdfExtractor, extractionAgent, properties, taxProfileRepository);

        // PdfExtractor returns dummy text for any file (content doesn't matter — AI agent is mocked)
        when(pdfExtractor.extract(any(MultipartFile.class))).thenReturn("dummy pdf text");
    }

    // -----------------------------------------------------------------------
    // Helpers

    private static ExtractedTaxFieldsDto fields(double income, double rrsp, double gains,
                                                 double cIncome, double cRrsp, double cGains) {
        return new ExtractedTaxFieldsDto(
                income, rrsp, gains,
                new ExtractedTaxFieldsDto.ConfidenceDto(cIncome, cRrsp, cGains));
    }

    private static MockMultipartFile pdf(String name) {
        return new MockMultipartFile("file", name, "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});
    }

    // -----------------------------------------------------------------------
    // Tests

    @Test
    @DisplayName("two T4 slips: employment incomes are summed")
    void twoT4sIncomeIsSummed() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(48_200.0, 3_000.0, 0.0, 0.95, 0.90, 1.0))
                .thenReturn(fields(31_500.0, 1_500.0, 0.0, 0.92, 0.88, 1.0));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("employer-a.pdf"), pdf("employer-b.pdf")), "2024");

        assertThat(merged.employmentIncome()).isEqualTo(79_700.0);
    }

    @Test
    @DisplayName("two slips: RRSP contributions are summed")
    void twoSlipsRrspIsSummed() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(48_200.0, 3_000.0, 0.0, 0.95, 0.90, 1.0))
                .thenReturn(fields(31_500.0, 1_500.0, 0.0, 0.92, 0.88, 1.0));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("t4-1.pdf"), pdf("t4-2.pdf")), "2024");

        assertThat(merged.rrspContribution()).isEqualTo(4_500.0);
    }

    @Test
    @DisplayName("two slips: capital gains are summed")
    void twoSlipsCapitalGainsAreSummed() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(50_000.0, 0.0, 1_200.0, 0.94, 1.0, 0.89))
                .thenReturn(fields(0.0,       0.0, 800.0,  1.0,  1.0, 0.91));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("t5-a.pdf"), pdf("t5-b.pdf")), "2024");

        assertThat(merged.capitalGains()).isEqualTo(2_000.0);
    }

    @Test
    @DisplayName("confidence score is the minimum across slips — most conservative wins")
    void confidenceIsMinimumAcrossSlips() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(48_200.0, 3_000.0, 0.0, 0.95, 0.90, 1.0))
                .thenReturn(fields(31_500.0, 1_500.0, 0.0, 0.70, 0.55, 1.0));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("a.pdf"), pdf("b.pdf")), "2024");

        assertThat(merged.confidenceScores().employmentIncome()).isEqualTo(0.70);
        assertThat(merged.confidenceScores().rrspContribution()).isEqualTo(0.55);
        assertThat(merged.confidenceScores().capitalGains()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("null AI fields are treated as 0.0 and do not cause NullPointerException")
    void nullFieldsCoercedToZero() {
        // Simulate a slip where AI returns null for capital gains (field not found)
        ExtractedTaxFieldsDto slipWithNulls = new ExtractedTaxFieldsDto(
                52_000.0, 4_000.0, null,
                new ExtractedTaxFieldsDto.ConfidenceDto(0.93, 0.88, 0.0));

        when(extractionAgent.extract(anyString()))
                .thenReturn(slipWithNulls)
                .thenReturn(fields(28_000.0, 2_000.0, 500.0, 0.91, 0.85, 0.82));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("t4.pdf"), pdf("t5.pdf")), "2024");

        assertThat(merged.employmentIncome()).isEqualTo(80_000.0);
        assertThat(merged.capitalGains()).isEqualTo(500.0); // null coerced to 0 + 500
    }

    @Test
    @DisplayName("three slips: all additive fields accumulate correctly")
    void threeSlipsAccumulateCorrectly() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(40_000.0, 2_000.0, 0.0, 0.96, 0.92, 1.0))
                .thenReturn(fields(30_000.0, 1_500.0, 0.0, 0.94, 0.88, 1.0))
                .thenReturn(fields(15_000.0, 500.0,  0.0, 0.91, 0.85, 1.0));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("t4-1.pdf"), pdf("t4-2.pdf"), pdf("t4-3.pdf")), "2024");

        assertThat(merged.employmentIncome()).isEqualTo(85_000.0);
        assertThat(merged.rrspContribution()).isEqualTo(4_000.0);
    }

    @Test
    @DisplayName("merged profile has a non-null profileId assigned")
    void mergedProfileHasProfileId() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(50_000.0, 3_000.0, 0.0, 0.95, 0.90, 1.0))
                .thenReturn(fields(25_000.0, 1_000.0, 0.0, 0.93, 0.87, 1.0));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("a.pdf"), pdf("b.pdf")), "2024");

        assertThat(merged.profileId()).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("merged profile preserves the supplied tax year")
    void mergedProfilePreservesTaxYear() {
        when(extractionAgent.extract(anyString()))
                .thenReturn(fields(50_000.0, 3_000.0, 0.0, 0.95, 0.90, 1.0))
                .thenReturn(fields(25_000.0, 1_000.0, 0.0, 0.93, 0.87, 1.0));

        TaxProfile merged = service.extractAndMerge(
                List.of(pdf("a.pdf"), pdf("b.pdf")), "2023");

        assertThat(merged.taxYear()).isEqualTo("2023");
    }
}
