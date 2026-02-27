package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.dto.CraAuthRequest;
import com.devlawal.wealthsimpleatie.dto.CraImportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Orchestrates CRA data import for a taxpayer.
 *
 * Two modes controlled by {@code atie.cra.mock-mode} property:
 *
 *  LIVE mode  (atie.cra.mock-mode=false):
 *    Delegates to CraAfrClient which calls CRA's Auto-fill My Return API.
 *    Requires a valid SIN + DOB + NETFILE Access Code from the taxpayer,
 *    and CRA NETFILE certification for the transmitter number.
 *
 *  MOCK mode  (atie.cra.mock-mode=true, default for dev):
 *    Returns hardcoded T4/RRSP values that mirror realistic CRA T1 data.
 *    Safe to use during development and testing without CRA credentials.
 */
@Service
public class CraImportService {

    private static final Logger log = LoggerFactory.getLogger(CraImportService.class);

    private final CraAfrClient craAfrClient;
    private final boolean mockMode;

    public CraImportService(
            CraAfrClient craAfrClient,
            @Value("${atie.cra.mock-mode:true}") boolean mockMode) {
        this.craAfrClient = craAfrClient;
        this.mockMode     = mockMode;
        log.info("CraImportService initialised — mockMode={}", mockMode);
    }

    /**
     * Pulls CRA tax data using the taxpayer's NETFILE Access Code.
     * In mock mode the auth credentials are ignored and simulated data is returned.
     *
     * @param auth  taxpayer credentials (SIN, DOB, NETFILE Access Code, tax year)
     * @return CRA-reported financial data for reconciliation
     */
    public CraImportDto importWithNac(CraAuthRequest auth) {
        if (mockMode) {
            log.info("MOCK mode — returning simulated CRA data for {}", auth.toMaskedString());
            return mockResponse(auth.taxYear());
        }

        log.info("LIVE mode — calling CRA AFR for {}", auth.toMaskedString());
        return craAfrClient.fetchTaxData(auth);
    }

    /**
     * Legacy convenience method used internally when only SIN + taxYear are known
     * and no NAC was supplied (e.g. automated batch runs in mock mode only).
     */
    public CraImportDto importForTaxpayer(String sin, String taxYear) {
        if (!mockMode) {
            throw new IllegalStateException(
                    "importForTaxpayer() without NAC is only permitted in mock mode. " +
                    "Use importWithNac(CraAuthRequest) in live mode.");
        }
        log.info("CRA mock import for SIN ending '{}', tax year {}",
                sin != null && sin.length() >= 3 ? "***-***-" + sin.substring(sin.length() - 3) : "***",
                taxYear);
        return mockResponse(taxYear);
    }

    // ── Mock data ────────────────────────────────────────────────────────────

    private CraImportDto mockResponse(String taxYear) {
        return new CraImportDto(
                48_200.00,          // employmentIncome  → T4 Box 14
                "Mock Employer Ltd.", // employerName
                9_800.00,           // incomeTaxDeducted → T4 Box 22
                3_500.00,           // rrspContribution
                29_210.00,          // rrspDeductionLimit (from NOA)
                1_200.00,           // capitalGains
                500.00,             // dividendIncome    → T5
                0.00,               // otherIncome       → T4A
                taxYear,
                "***",              // sinLast3 masked
                "MOCK"
        );
    }
}
