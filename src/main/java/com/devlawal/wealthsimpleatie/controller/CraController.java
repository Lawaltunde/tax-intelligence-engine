package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.CraAuthRequest;
import com.devlawal.wealthsimpleatie.dto.CraImportDto;
import com.devlawal.wealthsimpleatie.service.CraImportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles CRA data import requests.
 *
 * POST /api/v1/cra/import
 *   Authenticates with CRA using the taxpayer's NETFILE Access Code and
 *   returns their pre-populated T1 slip data (T4, RRSP, T5, etc.).
 *
 *   In mock mode (atie.cra.mock-mode=true) the credentials are ignored
 *   and realistic sample data is returned — safe for development.
 *
 *   In live mode the caller must supply a valid:
 *     - 9-digit SIN (no dashes)
 *     - Date of birth (yyyy-MM-dd)
 *     - 4-character NETFILE Access Code from their latest Notice of Assessment
 */
@RestController
@RequestMapping("/api/v1/cra")
public class CraController {

    private final CraImportService craImportService;

    public CraController(CraImportService craImportService) {
        this.craImportService = craImportService;
    }

    /**
     * POST /api/v1/cra/import
     *
     * Pulls the taxpayer's CRA slip data using their NETFILE Access Code.
     *
     * Request body example:
     * {
     *   "sin":               "123456789",
     *   "dateOfBirth":       "1985-04-12",
     *   "netfileAccessCode": "AB12",
     *   "taxYear":           "2024"
     * }
     */
    @PostMapping("/import")
    public ResponseEntity<CraImportDto> importFromCra(@Valid @RequestBody CraAuthRequest auth) {
        CraImportDto data = craImportService.importWithNac(auth);
        return ResponseEntity.ok(data);
    }
}
