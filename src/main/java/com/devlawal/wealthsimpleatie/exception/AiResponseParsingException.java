package com.devlawal.wealthsimpleatie.exception;

/**
 * Thrown when the AI returns a response that cannot be parsed into the expected JSON schema.
 */
public class AiResponseParsingException extends RuntimeException {

    public AiResponseParsingException(String message) {
        super(message);
    }

    public AiResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
