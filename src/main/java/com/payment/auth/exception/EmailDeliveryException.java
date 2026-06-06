package com.payment.auth.exception;
public class EmailDeliveryException extends Exception {
    public EmailDeliveryException(String message) { super(message); }
    public EmailDeliveryException(String message, Throwable cause) { super(message, cause); }
}
