package com.payment.auth.exception;
public class OtpLockoutException extends Exception {
    public OtpLockoutException(String message) { super(message); }
    public OtpLockoutException(String message, Throwable cause) { super(message, cause); }
}
