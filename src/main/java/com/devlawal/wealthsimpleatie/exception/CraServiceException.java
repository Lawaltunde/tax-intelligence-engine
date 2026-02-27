package com.devlawal.wealthsimpleatie.exception;

/**
 * Thrown when the CRA AFR service is unavailable or returns an unexpected error.
 * Maps to HTTP 502 Bad Gateway — the upstream (CRA) is the fault, not the caller.
 */
public class CraServiceException extends RuntimeException {

    public CraServiceException(String message) {
        super(message);
    }

    public CraServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
