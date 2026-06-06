package com.payment.exception;

/**
 * Thrown when the payment gateway declines the card or is unreachable.
 * Caught by PaymentProcessor retry loop — re-thrown after maxRetries exhausted.
 *
 * Jira: SCRUM-8 | PR: #847
 */
public class PaymentGatewayException extends RuntimeException {
    private final String errorCode;

    public PaymentGatewayException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentGatewayException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
