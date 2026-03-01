package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.TaxSlipExtractionAgent;
import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.dto.ExtractedTaxFieldsDto;
import com.devlawal.wealthsimpleatie.exception.DocumentExtractionException;
import com.devlawal.wealthsimpleatie.model.ConfidenceScores;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.TaxProfileRepository;
import com.devlawal.wealthsimpleatie.util.PdfTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
        if (taxYear == null || taxYear.isBlank() || !taxYear.matches("\\d{4}")) {
            throw new DocumentExtractionException(
                    "Invalid taxYear '%s' — expected a 4-digit year (e.g. 2024).".formatted(taxYear));
        }
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

        log.info("Extraction complete and persisted: profileId={}, taxYear={}", profile.profileId(), profile.taxYear());
        return profile;
    }

    // -----------------------------------------------------------------------

    /**
     * Extracts fields from multiple PDF slips belonging to the same tax year and merges
     * them into a single TaxProfile.
     *
     * Merge rules (designed to be accurate for the multi-employer / multi-slip scenario):
     *   - employmentIncome   — summed  (two jobs = two T4s, both contribute to total income)
     *   - rrspContribution   — summed  (contributions reported on multiple slips)
     *   - capitalGains       — summed  (multiple investment slips)
     *   - confidenceScores   — minimum per-field (most conservative: weakest extraction drives trust)
     *
     * A single merged TaxProfile is saved to the database.
     *
     * @param files   two or more PDF slips for the same tax year
     * @param taxYear tax year all slips belong to
     * @return merged TaxProfile
     */
    public TaxProfile extractAndMerge(List<MultipartFile> files, String taxYear) {
        if (files == null || files.isEmpty()) {
            throw new DocumentExtractionException(
                    "files must be a non-empty list of MultipartFile.");
        }
        for (MultipartFile f : files) {
            if (f == null) {
                throw new DocumentExtractionException(
                        "files must be a non-empty list of MultipartFile — null element found.");
            }
        }
        if (taxYear == null || taxYear.isBlank() || !taxYear.matches("\\d{4}")) {
            throw new DocumentExtractionException(
                    "Invalid taxYear '%s' — expected a 4-digit year (e.g. 2024).".formatted(taxYear));
        }
        log.info("Starting multi-slip extraction for {} files, tax year {}", files.size(), taxYear);

        double totalEmploymentIncome = 0.0;
        double totalRrspContribution = 0.0;
        double totalCapitalGains     = 0.0;

        // Confidence: start at 1.0, take the minimum across slips so the merged score
        // reflects the least-confident extraction (conservative and honest).
        double minConfEmployment = 1.0;
        double minConfRrsp       = 1.0;
        double minConfCapital    = 1.0;

        for (MultipartFile file : files) {
            log.info("  Extracting slip: {}", file.getOriginalFilename());

            String documentText = pdfExtractor.extract(file);
            ExtractedTaxFieldsDto extracted = extractionAgent.extract(documentText);

            totalEmploymentIncome += coerce(extracted.employmentIncome());
            totalRrspContribution += coerce(extracted.rrspContribution());
            totalCapitalGains     += coerce(extracted.capitalGains());

            ExtractedTaxFieldsDto.ConfidenceDto conf =
                    extracted.confidence() != null ? extracted.confidence()
                                                   : ExtractedTaxFieldsDto.ConfidenceDto.empty();

            minConfEmployment = Math.min(minConfEmployment, coerce(conf.employmentIncome()));
            minConfRrsp       = Math.min(minConfRrsp,       coerce(conf.rrspContribution()));
            minConfCapital    = Math.min(minConfCapital,     coerce(conf.capitalGains()));
        }

        ConfidenceScores mergedScores = new ConfidenceScores(
                minConfEmployment, minConfRrsp, minConfCapital);

        logLowConfidenceFields(mergedScores);

        TaxProfile profile = TaxProfile.builder()
                .profileId(UUID.randomUUID().toString())
                .taxYear(taxYear)
                .employmentIncome(totalEmploymentIncome)
                .rrspContribution(totalRrspContribution)
                .capitalGains(totalCapitalGains)
                .confidenceScores(mergedScores)
                .build();

        taxProfileRepository.save(profile);

        log.info("Multi-slip merge complete: profileId={}, slips={}, totalIncome={}",
                profile.profileId(), files.size(), totalEmploymentIncome);
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
