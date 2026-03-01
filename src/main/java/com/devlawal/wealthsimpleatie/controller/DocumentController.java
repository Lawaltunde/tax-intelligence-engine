package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.ExtractedProfileDto;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.service.DocumentExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Handles PDF tax slip upload and extraction.
 * No business logic — delegates entirely to DocumentExtractionService.
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentExtractionService extractionService;

    public DocumentController(DocumentExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    /**
     * POST /api/v1/documents/extract
     *
     * Accepts one or more PDF tax slips for the same tax year and returns a single merged
     * profile DTO with confidence scores.  Multiple T4s (e.g. two employers) are supported:
     * additive fields (income, contributions, gains) are summed; confidence scores are
     * averaged across slips.
     *
     * @param files   one or more uploaded PDFs (T4, T5, RRSP, T2202, etc.)
     * @param taxYear tax year the documents belong to (e.g. 2024)
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExtractedProfileDto> extract(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam(value = "taxYear", defaultValue = "2024") String taxYear) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        for (MultipartFile f : files) {
            if (f.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(f.getContentType())) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }
        }

        TaxProfile profile = (files.size() == 1)
                ? extractionService.extract(files.get(0), taxYear)
                : extractionService.extractAndMerge(files, taxYear);

        return ResponseEntity.ok(ExtractedProfileDto.from(profile));
    }
}
