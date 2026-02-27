package com.devlawal.wealthsimpleatie.exception;

/**
 * Thrown when the reconciliation engine receives invalid or missing data.
 */
public class ReconciliationException extends RuntimeException {

    public ReconciliationException(String message) {
        super(message);
    }

    public ReconciliationException(String message, Throwable cause) {
        super(message, cause);
    }
}
