package com.devlawal.wealthsimpleatie.exception;

/**
 * Thrown when CRA rejects the taxpayer's credentials (SIN, DOB, or NETFILE Access Code).
 * Maps to HTTP 401 Unauthorized.
 */
public class CraAuthenticationException extends RuntimeException {

    public CraAuthenticationException(String message) {
        super(message);
    }

    public CraAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
