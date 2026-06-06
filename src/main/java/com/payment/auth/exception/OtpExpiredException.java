package com.payment.auth.exception;
public class OtpExpiredException extends Exception {
    public OtpExpiredException(String message) { super(message); }
    public OtpExpiredException(String message, Throwable cause) { super(message, cause); }
}
