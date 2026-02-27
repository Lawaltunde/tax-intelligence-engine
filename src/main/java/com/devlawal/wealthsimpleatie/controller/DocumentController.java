package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.ExtractedProfileDto;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.service.DocumentExtractionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * Accepts a PDF tax slip and returns a structured profile DTO with confidence scores.
     * The returned profileId should be stored by the frontend and passed to all subsequent calls.
     *
     * @param file    uploaded PDF (T4, T5, RRSP, T2202, etc.)
     * @param taxYear tax year the document belongs to (e.g. 2024)
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExtractedProfileDto> extract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taxYear", defaultValue = "2024") String taxYear) {

        TaxProfile profile = extractionService.extract(file, taxYear);
        return ResponseEntity.ok(ExtractedProfileDto.from(profile));
    }
}
