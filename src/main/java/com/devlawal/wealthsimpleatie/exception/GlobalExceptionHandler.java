package com.devlawal.wealthsimpleatie.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Centralised exception-to-HTTP-response mapping.
 * Uses RFC 7807 ProblemDetail for consistent error payloads.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DocumentExtractionException.class)
    public ProblemDetail handleDocumentExtraction(DocumentExtractionException ex) {
        log.error("Document extraction failure: {}", ex.getMessage(), ex);
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "document-extraction-error",
                "Document Extraction Failed", ex.getMessage());
    }

    @ExceptionHandler(AiResponseParsingException.class)
    public ProblemDetail handleAiParsing(AiResponseParsingException ex) {
        log.error("AI response parsing failure: {}", ex.getMessage(), ex);
        return problem(HttpStatus.BAD_GATEWAY, "ai-response-parse-error",
                "AI Response Parsing Failed", ex.getMessage());
    }

    @ExceptionHandler(ReconciliationException.class)
    public ProblemDetail handleReconciliation(ReconciliationException ex) {
        log.error("Reconciliation failure: {}", ex.getMessage(), ex);
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "reconciliation-error",
                "Reconciliation Failed", ex.getMessage());
    }

    @ExceptionHandler(CraAuthenticationException.class)
    public ProblemDetail handleCraAuth(CraAuthenticationException ex) {
        // Log the detail at DEBUG only — the message may contain credential hints (SIN, NAC, DOB).
        // Never echo raw exception text to the client response.
        log.warn("CRA authentication rejected (see DEBUG for detail)");
        log.debug("CRA authentication detail: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "cra-auth-error",
                "CRA Authentication Failed",
                "CRA authentication failed. Please verify your credentials and try again.");
    }

    @ExceptionHandler(CraServiceException.class)
    public ProblemDetail handleCraService(CraServiceException ex) {
        log.error("CRA service error: {}", ex.getMessage(), ex);
        return problem(HttpStatus.BAD_GATEWAY, "cra-service-error",
                "CRA Service Unavailable", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return problem(HttpStatus.BAD_REQUEST, "validation-error", "Request Validation Failed", detail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "Internal Server Error", "An unexpected error occurred.");
    }

    // -----------------------------------------------------------------------

    private ProblemDetail problem(HttpStatus status, String errorCode, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create("https://atie.devlawal.com/errors/" + errorCode));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
