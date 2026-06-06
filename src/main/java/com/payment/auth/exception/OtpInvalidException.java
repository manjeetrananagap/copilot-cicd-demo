package com.payment.auth.exception;
public class OtpInvalidException extends Exception {
    public OtpInvalidException(String message) { super(message); }
    public OtpInvalidException(String message, Throwable cause) { super(message, cause); }
}
