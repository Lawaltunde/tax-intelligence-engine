package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.TaxSlipExtractionAgent;
import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.dto.ExtractedTaxFieldsDto;
import com.devlawal.wealthsimpleatie.model.ConfidenceScores;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.TaxProfileRepository;
import com.devlawal.wealthsimpleatie.util.PdfTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Orchestrates the full document ingestion pipeline:
 *   PDF → raw text → AI extraction → validation → TaxProfile
 *
 * LLM boundary: extraction only.
 * Deterministic: all null-coercion, confidence gating, normalization.
 */
@Service
public class DocumentExtractionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentExtractionService.class);

    private final PdfTextExtractor pdfExtractor;
    private final TaxSlipExtractionAgent extractionAgent;
    private final AtieProperties properties;
    private final TaxProfileRepository taxProfileRepository;

    public DocumentExtractionService(PdfTextExtractor pdfExtractor,
                                     TaxSlipExtractionAgent extractionAgent,
                                     AtieProperties properties,
                                     TaxProfileRepository taxProfileRepository) {
        this.pdfExtractor         = pdfExtractor;
        this.extractionAgent      = extractionAgent;
        this.properties           = properties;
        this.taxProfileRepository = taxProfileRepository;
    }

    /**
     * Processes an uploaded tax slip PDF and returns a normalised TaxProfile.
     *
     * @param file       uploaded PDF (T4, T5, RRSP, T2202, etc.)
     * @param taxYear    tax year the document belongs to (e.g. "2024")
     * @return normalised TaxProfile with per-field confidence scores
     */
    public TaxProfile extract(MultipartFile file, String taxYear) {
        log.info("Starting extraction for file '{}', tax year {}", file.getOriginalFilename(), taxYear);

        // 1. Convert PDF to text
        String documentText = pdfExtractor.extract(file);

        // 2. Send to AI agent for structured extraction
        ExtractedTaxFieldsDto extracted = extractionAgent.extract(documentText);

        // 3. Deterministic normalization — replace nulls with 0.0
        double employmentIncome = coerce(extracted.employmentIncome());
        double rrspContribution = coerce(extracted.rrspContribution());
        double capitalGains     = coerce(extracted.capitalGains());

        // 4. Build confidence scores
        ExtractedTaxFieldsDto.ConfidenceDto conf =
                extracted.confidence() != null ? extracted.confidence()
                                               : ExtractedTaxFieldsDto.ConfidenceDto.empty();

        ConfidenceScores scores = new ConfidenceScores(
                coerce(conf.employmentIncome()),
                coerce(conf.rrspContribution()),
                coerce(conf.capitalGains())
        );

        // 5. Log low-confidence fields for audit trail
        logLowConfidenceFields(scores);

        TaxProfile profile = TaxProfile.builder()
                .profileId(UUID.randomUUID().toString())
                .taxYear(taxYear)
                .employmentIncome(employmentIncome)
                .rrspContribution(rrspContribution)
                .capitalGains(capitalGains)
                .confidenceScores(scores)
                .build();

        // 6. Persist to database
        taxProfileRepository.save(profile);

        log.info("Extraction complete and persisted: {}", profile);
        return profile;
    }

    // -----------------------------------------------------------------------

    private double coerce(Double value) {
        return value != null ? value : 0.0;
    }

    private void logLowConfidenceFields(ConfidenceScores scores) {
        double threshold = properties.confidence().reviewThreshold();
        if (scores.employmentIncome() < threshold) log.warn("Low confidence on employmentIncome: {}", scores.employmentIncome());
        if (scores.rrspContribution() < threshold) log.warn("Low confidence on rrspContribution: {}", scores.rrspContribution());
        if (scores.capitalGains()     < threshold) log.warn("Low confidence on capitalGains: {}", scores.capitalGains());
    }
}
