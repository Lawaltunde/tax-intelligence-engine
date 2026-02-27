package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.dto.CraAuthRequest;
import com.devlawal.wealthsimpleatie.dto.CraImportDto;
import com.devlawal.wealthsimpleatie.exception.CraAuthenticationException;
import com.devlawal.wealthsimpleatie.exception.CraServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP client for the CRA Auto-fill My Return (AFR) API.
 *
 * Authentication flow:
 *  1. Caller supplies SIN + DOB + NETFILE Access Code (NAC)
 *  2. We POST to CRA's token endpoint to exchange credentials for a session token
 *  3. We use that token to GET the taxpayer's pre-populated T1 data
 *
 * CRA AFR API reference:
 *  https://www.canada.ca/en/revenue-agency/services/e-services/digital-services-individuals/netfile-overview/certified-software-netfile-program.html
 *
 * NOTE: This client requires NETFILE certification from CRA before live use.
 *       Without certification, the baseUrl should point to CRA's test environment.
 *       Set atie.cra.base-url and atie.cra.transmitter-number in application.properties.
 */
@Component
public class CraAfrClient {

    private static final Logger log = LoggerFactory.getLogger(CraAfrClient.class);

    private static final String TOKEN_PATH  = "/efile/token";
    private static final String AFR_PATH    = "/efile/autofill";

    private final RestClient restClient;
    private final String transmitterNumber;
    private final boolean certificationMode;

    public CraAfrClient(
            @Value("${atie.cra.base-url}") String baseUrl,
            @Value("${atie.cra.transmitter-number:T99999999}") String transmitterNumber,
            @Value("${atie.cra.certification-mode:false}") boolean certificationMode) {

        this.transmitterNumber  = transmitterNumber;
        this.certificationMode  = certificationMode;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT,       MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Transmitter-Number",  transmitterNumber)
                .build();

        log.info("CraAfrClient initialised — baseUrl={}, transmitter={}, certificationMode={}",
                baseUrl, transmitterNumber, certificationMode);
    }

    /**
     * Authenticates with CRA using the taxpayer's NAC and pulls their AFR data.
     *
     * @param auth  taxpayer credentials (SIN, DOB, NETFILE Access Code, tax year)
     * @return populated CraImportDto with all slip data for the requested tax year
     * @throws CraAuthenticationException if NAC / SIN / DOB are rejected by CRA (401/403)
     * @throws CraServiceException        if CRA service is unavailable (5xx)
     */
    public CraImportDto fetchTaxData(CraAuthRequest auth) {
        log.info("Initiating CRA AFR pull: {}", auth.toMaskedString());

        String sessionToken = authenticate(auth);
        return fetchAfrData(sessionToken, auth.taxYear());
    }

    // ── Step 1: exchange credentials for a session token ────────────────────

    private String authenticate(CraAuthRequest auth) {
        log.debug("Requesting CRA session token for NAC exchange");

        try {
            Map<?, ?> tokenResponse = restClient.post()
                    .uri(TOKEN_PATH)
                    .body(Map.of(
                            "sin",              auth.sin(),
                            "dateOfBirth",      auth.dateOfBirth(),
                            "netfileAccessCode", auth.netfileAccessCode(),
                            "taxYear",          auth.taxYear()
                    ))
                    .retrieve()
                    .body(Map.class);

            if (tokenResponse == null || !tokenResponse.containsKey("sessionToken")) {
                throw new CraServiceException("CRA token response was empty or malformed");
            }

            log.debug("CRA session token obtained successfully");
            return (String) tokenResponse.get("sessionToken");

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new CraAuthenticationException(
                        "CRA rejected the credentials — verify SIN, date of birth, and NETFILE Access Code. " +
                        "The NAC expires after each NOA is issued.");
            }
            throw new CraServiceException("CRA authentication failed: " + e.getMessage());
        }
    }

    // ── Step 2: use session token to fetch AFR data ──────────────────────────

    private CraImportDto fetchAfrData(String sessionToken, String taxYear) {
        log.debug("Fetching CRA AFR data for tax year {}", taxYear);

        try {
            CraImportDto data = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(AFR_PATH)
                            .queryParam("taxYear", taxYear)
                            .build())
                    .header("X-Session-Token", sessionToken)
                    .retrieve()
                    .body(CraImportDto.class);

            if (data == null) {
                throw new CraServiceException("CRA AFR response was empty for tax year " + taxYear);
            }

            log.info("CRA AFR pull successful — dataSource={}, sinLast3={}",
                    data.dataSource(), data.sinLast3());
            return data;

        } catch (HttpClientErrorException e) {
            throw new CraServiceException("CRA AFR data fetch failed: " + e.getMessage());
        }
    }
}
